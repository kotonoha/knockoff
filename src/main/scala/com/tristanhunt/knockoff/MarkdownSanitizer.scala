/*
 * Copyright 2012-2013 eiennohito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tristanhunt.knockoff

import org.owasp.html.HtmlSanitizer
import java.util
import scala.collection.mutable.ListBuffer
import scala.util.parsing.input.Position
import scala.util.DynamicVariable

/**
 * @author eiennohito
 * @since 20.04.13 
 */

class SanitizedNodeBuilder(converter: SpanConverter) extends NodeSeqConstructor {
  val random = new util.Random()
  def text(text: String) {
    val nodes = converter(TextChunk(text))
    val node = nodes match {
      case Seq(Text(content)) => //has nothing interesting here
        xml.Text(content)
      case _ =>
        val id = random.nextLong().toHexString
        spanBuffer += id -> nodes
        <span id={id}></span>
    }
    stack.head.append(Seq(node))
  }

  private val spanBuffer = new ListBuffer[(String, Seq[Span])]()

  def spans = spanBuffer
}

case class UnlinkedPosition(line: Int, column: Int, lineContents: String)

object Maker {
  def fromPos(p: Position) = {
    val cont = p.longString
    new UnlinkedPosition(p.line, p.column, cont.substring(0, cont.indexOf('\n')))
  }
}

case class SanitizationEvent(where: UnlinkedPosition, tag: String, attrs: List[String])

class SanitizationListener(sink: SanitizationEvent => Unit) {
  private var pos: UnlinkedPosition = null

  def publish(ev: SanitizationEvent) = {
    if (pos != null)
      sink(ev.copy(where = pos))
  }

  def position(p: Position) = pos = Maker.fromPos(p)
}

object SanitizationChangeSupport {
  private[knockoff] object listener extends DynamicVariable[SanitizationListener] (null)

  def withSink[T](sink: SanitizationEvent => Unit)(f: => T) {
    val lst = new SanitizationListener(sink)
    listener.withValue(lst)(f)
  }
}

class ChangeListenerScala {
  def discardedTag(context: AnyRef, elementName: String) {
    val info = SanitizationEvent(null, elementName, Nil)
    val listener = SanitizationChangeSupport.listener.value
    if (listener != null) listener.publish(info)
  }

  def discardedAttributes(context: AnyRef, tagName: String, attributeNames: Array[String]) {
    val info = SanitizationEvent(null, tagName, attributeNames.toList)
    val listener = SanitizationChangeSupport.listener.value
    if (listener != null) listener.publish(info)
  }
}

class KnockoffSanitizer(converter: SpanConverter) {

  val factory = Sanitizer.policyFactory

  def sanitized(s: String) = {
    val bldr = new SanitizedNodeBuilder(converter)
    val policy = if (SanitizationChangeSupport.listener.value == null)
      factory.apply(bldr)
    else {
      val lstnr = new ChangeListener(new ChangeListenerScala)
      factory.apply(bldr, lstnr, null)
    }
    HtmlSanitizer.sanitize(s, policy)
    val nodes = bldr.result
    //spans come from sanitizer free from html, no need to pass them in again
    val md = bldr.spans
    (nodes, md.toMap)
  }

  def sanitizeSpans(spans: Seq[Span], pos: Position): Seq[Span] = {
    if (pos != null && SanitizationChangeSupport.listener.value != null) {
      SanitizationChangeSupport.listener.value.position(pos)
    }
    spans.map {
      case HTMLSpan(text) =>
        val (nodes, md) = sanitized(text)
        SanitizedHtmlSpan(nodes, md)
      case Strong(s) => Strong(sanitizeSpans(s, null))
      case Emphasis(s) => Emphasis(sanitizeSpans(s, null))
      case Link(s, uri, d) => Link(sanitizeSpans(s, null), uri, d)
      case IndirectLink(s, d) => IndirectLink(sanitizeSpans(s, null), d)
      case ImageLink(s, uri, title) => ImageLink(sanitizeSpans(s, null), uri, title)
      case IndirectImageLink(s, d) => IndirectImageLink(sanitizeSpans(s, null), d)
      case x => x
    }
  }

  def sanitize[T <: Block](blocks: Seq[T]): Seq[T] = {
    blocks.map {
      //dealing on spans on blocks-of-spans
      case Paragraph(spans, pos) => Paragraph(sanitizeSpans(spans, pos), pos)
      case Header(lvl, spans, pos) => Header(lvl, sanitizeSpans(spans, pos), pos)
      //recursive call on blocks-of-blocks
      case OrderedItem(items, pos) => OrderedItem(sanitize(items), pos)
      case UnorderedItem(items, pos) => UnorderedItem(sanitize(items), pos)
      case Blockquote(items, pos) => Blockquote(sanitize(items), pos)
      case OrderedList(items) => OrderedList(sanitize(items))
      case UnorderedList(items) => UnorderedList(sanitize(items))
      //and the culprit - sanitizing it
      case HTMLBlock(html, pos) =>
        val (nodes, md) = sanitized(html)
        SanitizedHtmlBlock(nodes, md, pos)
      case x => x
    }.asInstanceOf[Seq[T]]
  }
}
