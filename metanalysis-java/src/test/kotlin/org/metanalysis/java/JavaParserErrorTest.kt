/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.java

import org.junit.Test
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable

import org.metanalysis.core.parsing.SyntaxErrorException
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.sourceFileOf

import kotlin.test.assertFailsWith

class JavaParserErrorTest : JavaParserTest() {
    @Test fun `test parse invalid source throws`() {
        val source = "cla Main { int i = &@*; { class K; interface {}"
        assertFailsWith<SyntaxErrorException> {
            parser.parse(source)
        }
    }

    @Test fun `test parse duplicated members in class throws`() {
        val source = "class Main { int i = 2; int i = 3; }"
        assertFailsWith<SyntaxErrorException> {
            parser.parse(source)
        }
    }

    @Test fun `test initializers not supported`() {
        val source = """
        class Type {
            int i;
            {
                i = 2;
            }
        }
        """
        val expected = sourceFileOf(Type(name = "Type", members = setOf(
                Variable(name = "i")
        )))
        val actual = parser.parse(source)
        assertEquals(expected, actual)
    }
}
