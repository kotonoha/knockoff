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

import scala.util.parsing.input.Position

/**
 * @author eiennohito
 * @since 20.04.13 
 */

object SpanWalker {
  def spans(blocks: Traversable[Block]): Stream[(Span, Position)] = {
    blocks.toStream.flatMap {
      case Paragraph(spans, pos) => spans.toStream.map(x => (x, pos))
      case Header(lvl, spans, pos) => spans.toStream.map(x => (x, pos))
      case SanitizedHtmlBlock(_, md, pos) => md.values.toStream.flatten.map(x => (x, pos))
      case OrderedItem(items, _) => spans(items)
      case UnorderedItem(items, _) => spans(items)
      case Blockquote(items, _) => spans(items)
      case OrderedList(items) => spans(items)
      case UnorderedList(items) => spans(items)
      case _ => Stream.empty
    }
  }
}
