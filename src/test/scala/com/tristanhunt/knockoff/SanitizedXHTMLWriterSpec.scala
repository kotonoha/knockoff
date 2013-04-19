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

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author eiennohito
 * @since 19.04.13 
 */

class SanitizedXHTMLWriterSpec extends FunSpec with ShouldMatchers {

  val parser = new Discounter {}
  val writer = new XHTMLWriter with SanitizedXHTMLWriter

  describe("SanitizedXHTMLWriter") {
    it ("shouldn't output script tags out") {
      val s = "it's a xss<script>alert('xss!')</script>"
      val nodes = parser.knockoff(s)
      val ns = writer.toXML(nodes)
      ns.toString() should be ("<p>it's a xss</p>")
    }

    it ("should leave out good content") {
      val s = "<i class='icon-stop'></i><script>alert('xss!')</script>"
      val nodes = parser.knockoff(s)
      val ns = writer.toXML(nodes)
      ns.toString().replace("\"", "'") should be ("<i class='icon-stop'></i>")
    }
  }

}
