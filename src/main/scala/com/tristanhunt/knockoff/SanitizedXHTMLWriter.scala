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

import org.owasp.html.{HtmlSanitizer, HtmlStreamEventReceiver, HtmlPolicyBuilder}
import java.util
import scala.xml._
import scala.collection.mutable
import scala.xml.Group
import scala.collection.mutable.ListBuffer

/**
 * @author eiennohito
 * @since 19.04.13 
 */

class Level(val name: String, val attrs: MetaData) {
  private val children: mutable.ListBuffer[Node] = new ListBuffer[Node]

  def append(other: Level) = children += other.make

  def append(ns: Seq[Node]) = children ++= ns

  def make: Node = Elem(null, name, attrs, TopScope, false, children.result(): _*)
}

class UnbalancedTagException(msg: String) extends RuntimeException(msg)

class NodeSeqBuilder(writer: XHTMLWriter) extends HtmlStreamEventReceiver {
  def openDocument() {
    stack.push(new Level("div", Null))
  }

  def closeDocument() {}

  def openTag(elementName: String, attrs: util.List[String]) {
    import scala.collection.JavaConversions._
    val attr = attrs.grouped(2).foldRight[MetaData](Null) { (attr, tail) =>
      new UnprefixedAttribute(attr(0), attr(1), tail)
    }
    stack.push(new Level(elementName, attr))
  }

  def closeTag(elementName: String) {
    val item = stack.pop()
    if (item.name != elementName) {
      throw new UnbalancedTagException(elementName)
    }
    stack.head.append(item)
  }

  def text(text: String) {
    val converter = new SpanConverter(Nil)
    val tree = converter(TextChunk(text))
    val nodes = tree.map(writer.spanToXHTML(_))
    stack.head.append(nodes)
  }

  val stack = new mutable.Stack[Level]()

  def result: NodeSeq = stack.head.make.child
}

trait SanitizedXHTMLWriter { self: XHTMLWriter =>

  val blockParser = new ChunkParser

  val sanitizer = {
    new HtmlPolicyBuilder()
      .allowCommonBlockElements()
      .allowCommonInlineFormattingElements()
      .allowElements("tr", "td", "table", "thead", "tbody", "tfooter")
      .allowAttributes("class").globally()
      .toFactory
  }

  override def htmlSpanToXHTML(html: String): Node = {
    val bldr = new NodeSeqBuilder(this)
    val policy = sanitizer.apply(bldr)
    HtmlSanitizer.sanitize(html, policy)
    Group(bldr.result)
  }

  override def htmlBlockToXHTML(html: String) = {
    val bldr = new NodeSeqBuilder(this)
    val policy = sanitizer.apply(bldr)
    HtmlSanitizer.sanitize(html, policy)
    Group(bldr.result)
  }
}
