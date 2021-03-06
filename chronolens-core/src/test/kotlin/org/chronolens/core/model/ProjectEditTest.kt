/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.model

import org.chronolens.core.model.ProjectEdit.Companion.diff
import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.project
import org.junit.Test

class ProjectEditTest {
    private fun assertDiff(before: Project, after: Project) {
        val edits = before.diff(after)
        val actualAfter = Project.of(before.sources)
        actualAfter.apply(edits)
        assertEquals(after, actualAfter)
    }

    @Test
    fun `test diff equal projects`() {
        val before = project {
            sourceFile("src/Main.java") {
                type("Main") {
                    function("getVersion(String)") {
                        parameters("name")
                    }
                    variable("VERSION") {
                        modifiers("final", "static")
                        +"1"
                    }
                }
            }
        }

        val after = project {
            sourceFile("src/Main.java") {
                type("Main") {
                    function("getVersion(String)") {
                        parameters("name")
                    }
                    variable("VERSION") {
                        modifiers("final", "static")
                        +"1"
                    }
                }
            }
        }

        assertDiff(before, after)
    }


    @Test fun `test diff change type modifiers`() {
        val before = project {
            sourceFile("src/Main.java") {
                type("Main") {}
            }
        }

        val after = project {
            sourceFile("src/Main.java") {
                type("Main") {
                    modifiers("interface")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change type supertypes`() {
        val before = project {
            sourceFile("src/Main.java") {
                type("Main") {}
            }
        }

        val after = project {
            sourceFile("src/Main.java") {
                type("Main") {
                    supertypes("Object")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change function modifiers`() {
        val before = project {
            sourceFile("src/Test.java") {
                function("getVersion()") {}
            }
        }

        val after = project {
            sourceFile("src/Test.java") {
                function("getVersion()") {
                    modifiers("abstract")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change function parameters`() {
        val before = project {
            sourceFile("src/Test.java") {
                function("getVersion(String, int)") {
                    parameters("name", "revision")
                }
            }
        }

        val after = project {
            sourceFile("src/Test.java") {
                function("getVersion(String, int)") {
                    parameters("className", "revision")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change function body`() {
        val before = project {
            sourceFile("src/Test.java") {
                function("getVersion()") { +"DEBUG" }
            }
        }

        val after = project {
            sourceFile("src/Test.java") {
                function("getVersion()") { +"RELEASE" }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change variable modifiers`() {
        val before = project {
            sourceFile("src/Test.java") {
                variable("version") {
                    modifiers("public")
                }
            }
        }

        val after = project {
            sourceFile("src/Test.java") {
                variable("version") {}
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change variable initializer`() {
        val before = project {
            sourceFile("src/Test.java") {
                variable("version") { +"DEBUG" }
            }
        }

        val after = project {
            sourceFile("src/Test.java") {
                variable("version") { +"RELEASE" }
            }
        }

        assertDiff(before, after)
    }
}
