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

package org.metanalysis.core.project

import org.junit.After
import org.junit.Before
import org.junit.Test

import org.metanalysis.core.delta.NodeSetEdit
import org.metanalysis.core.delta.SourceFileTransaction
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.project.PersistentProject.Companion.persist
import org.metanalysis.core.project.Project.HistoryEntry
import org.metanalysis.test.core.project.ProjectMock
import org.metanalysis.test.core.project.assertEquals

import java.io.File
import java.io.IOException
import java.util.Date

class PersistentProjectTest : ProjectTest() {
    override val expectedProject: ProjectMock = ProjectMock(
            files = setOf("src/Main.java", "setup.py"),
            models = mapOf(
                    "src/Main.java" to SourceFile(),
                    "setup.py" to SourceFile()
            ),
            histories = mapOf(
                    "src/Main.java" to listOf(
                            HistoryEntry(
                                    revision = "1",
                                    date = Date(),
                                    author = "user",
                                    transaction = SourceFileTransaction(listOf(
                                            NodeSetEdit.Add(Type("IClass")),
                                            NodeSetEdit.Remove<Type>("IClass")
                                    ))
                            )
                    ),
                    "setup.py" to listOf(
                            HistoryEntry(
                                    revision = "1",
                                    date = Date(),
                                    author = "user",
                                    transaction = null
                            )
                    )
            )
    )

    override lateinit var actualProject: PersistentProject

    @Before fun initProject() {
        actualProject = expectedProject.persist()
    }

    @Test(expected = IOException::class)
    fun `test get file model with invalid code throws`() {
        val path = "setup.py"
        File(".metanalysis/objects/$path/model.json").delete()
        actualProject.getFileModel(path)
    }

    @Test fun `test persist already persistent returns same project`() {
        assertEquals(actualProject, actualProject.persist())
    }

    @Test fun `test load already persisted returns equal project`() {
        assertEquals(actualProject, PersistentProject.load())
    }

    @After fun cleanProject() {
        PersistentProject.clean()
    }
}