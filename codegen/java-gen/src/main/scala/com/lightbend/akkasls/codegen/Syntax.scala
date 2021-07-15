/*
 * Copyright 2021 Lightbend Inc.
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

package com.lightbend.akkasls.codegen

object Syntax {

  val break = "\n"

  def indent(lines: Iterable[String], num: Int): String =
    indent(lines.mkString(break), num)

  def indent(str: String, num: Int): String =
    str
      .split(break)
      .zipWithIndex
      .collect {
        // don't indent first line and empty lines
        case (line, idx) if idx == 0 || line.trim.isEmpty => line
        case (line, _) => (" " * num) + line
      }
      .mkString(break)

}
