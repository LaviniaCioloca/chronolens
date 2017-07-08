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

package org.metanalysis.core.serialization

import org.junit.Test

import org.metanalysis.core.delta.FunctionTransaction
import org.metanalysis.core.delta.ListEdit
import org.metanalysis.core.delta.NodeSetEdit
import org.metanalysis.core.delta.SetEdit
import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.delta.TypeTransaction
import org.metanalysis.core.delta.VariableTransaction
import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.core.project.Project.HistoryEntry

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Date

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonDriverTest {
    private val data = SourceFileTransaction(listOf(
            NodeSetEdit.Add(Type(
                    name = "IClass",
                    supertypes = setOf("Interface", "Object"),
                    members = setOf(
                            Type(name = "InnerClass"),
                            Variable(
                                    name = "version",
                                    initializer = listOf("1")
                            ),
                            Function(
                                    signature = "getVersion()",
                                    body = listOf("1")
                            )
                    )
            )),
            NodeSetEdit.Remove<Function>("createIClass()"),
            NodeSetEdit.Change<Variable>(
                    identifier = "DEBUG",
                    transaction = VariableTransaction(
                            initializerEdits = listOf(
                                    ListEdit.Add(index = 0, value = "true")
                            )
                    )
            ),
            NodeSetEdit.Remove<Variable>("RELEASE"),
            NodeSetEdit.Change<Type>("Interface", TypeTransaction(
                    supertypeEdits = listOf(SetEdit.Remove(value = "Object")),
                    memberEdits = listOf(NodeSetEdit.Change<Function>(
                            identifier = "getVersion()",
                            transaction = FunctionTransaction(
                                    parameterEdits = listOf(
                                            ListEdit.Remove(index = 0)
                                    ),
                                    bodyEdits = listOf(
                                            ListEdit.Remove(index = 0)
                                    )
                            )
                    ))
            ))
    ))

    @Test fun `test deserialize invalid KClass throws`() {
        val src = "\"java.lang.String$\"".byteInputStream()
        assertFailsWith<JsonException> {
            JsonDriver.deserialize<KClass<*>>(src)
        }
    }

    @Test fun `test serialize class throws`() {
        val dst = ByteArrayOutputStream()
        assertFailsWith<JsonException> {
            JsonDriver.serialize(dst, javaClass.classLoader)
        }
    }

    @Test fun `test serialize and deserialize source file transaction`() {
        val out = ByteArrayOutputStream()
        JsonDriver.serialize(out, data)
        val src = out.toByteArray().inputStream()
        val actualData = JsonDriver.deserialize<SourceFileTransaction>(src)
        assertEquals(data, actualData)
    }

    @Test fun `test serialize and deserialize history`() {
        val history = (1 until 10).map { i ->
            HistoryEntry(
                    revision = "$i",
                    date = Date(i.toLong()),
                    author = "<author>",
                    transaction = null
            )
        }
        val out = ByteArrayOutputStream()
        JsonDriver.serialize(out, history)
        val src = out.toByteArray().inputStream()
        val actualHistory = JsonDriver.deserialize<Array<HistoryEntry>>(src)
                .asList()
        assertEquals(history, actualHistory)
    }

    @Test fun `test serialize to and deserialize from top-level file`() {
        val file = File("tmp.json")
        try {
            JsonDriver.serialize(file, data)
            val actualData = JsonDriver.deserialize<SourceFileTransaction>(file)
            assertEquals(data, actualData)
        } finally {
            file.delete()
        }
    }

    @Test fun `test serialize to and deserialize from nested file`() {
        val dir = "tmpdir"
        val file = File("$dir/tmp.json")
        try {
            JsonDriver.serialize(file, data)
            val actualData = JsonDriver.deserialize<SourceFileTransaction>(file)
            assertEquals(data, actualData)
        } finally {
            File(dir).deleteRecursively()
        }
    }

    /*@Test fun gen() {
        val url1 = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/826e565b7cfba8de05f9f652c1541df8e8e7efe2/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
        val url2 = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/5e946c270018c71bf25778bc2dc25e5a9dd809b0/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
    }*/
}
