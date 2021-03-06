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

package org.chronolens.core.repository

import org.chronolens.core.repository.PersistentRepository.Companion.persist
import org.chronolens.core.repository.PersistentRepository.ProgressListener
import org.chronolens.test.core.repository.assertEquals
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class PersistentRepositoryTest : RepositoryTest() {
    override fun createRepository(): PersistentRepository =
        InteractiveRepository.connect()?.persist()
            ?: fail("Couldn't connect to VCS repository!")

    @Test fun `test load after clean returns null`() {
        PersistentRepository.clean()
        assertNull(PersistentRepository.load())
    }

    @Test fun `test load returns equal repository`() {
        val expected = repository
        val actual = PersistentRepository.load()
            ?: fail("Couldn't load persisted repository!")
        assertEquals(expected, actual)
    }

    @Test fun `test persist already persisted returns same repository`() {
        val expected = repository
        val actual = repository.persist()
        assertEquals(expected, actual)
    }

    private enum class ProgressListenerState {
        IDLE, SNAPSHOT, TRANSIENT, HISTORY, DONE
    }

    @Test fun `test progress listener`() {
        val listener = object : ProgressListener {
            var state = ProgressListenerState.IDLE
                private set

            private val sources = mutableSetOf<String>()
            private val revisions = mutableListOf<String>()

            override fun onSnapshotStart(headId: String, sourceCount: Int) {
                sources += repository.listSources()
                assertEquals(ProgressListenerState.IDLE, state)
                assertEquals(repository.getHeadId(), headId)
                assertEquals(sources.size, sourceCount)
                state = ProgressListenerState.SNAPSHOT
            }

            override fun onSourcePersisted(path: String) {
                assertEquals(ProgressListenerState.SNAPSHOT, state)
                assertTrue(path in sources)
                sources -= path
            }

            override fun onSnapshotEnd() {
                assertEquals(ProgressListenerState.SNAPSHOT, state)
                assertTrue(sources.isEmpty())
                state = ProgressListenerState.TRANSIENT
            }

            override fun onHistoryStart(revisionCount: Int) {
                revisions += repository.listRevisions()
                revisions.reverse()
                assertEquals(ProgressListenerState.TRANSIENT, state)
                assertEquals(revisions.size, revisionCount)
                state = ProgressListenerState.HISTORY
            }

            override fun onTransactionPersisted(id: String) {
                assertEquals(ProgressListenerState.HISTORY, state)
                assertEquals(revisions.last(), id)
                revisions.removeAt(revisions.size - 1)
            }

            override fun onHistoryEnd() {
                assertEquals(ProgressListenerState.HISTORY, state)
                assertTrue(revisions.isEmpty())
                state = ProgressListenerState.DONE
            }
        }

        InteractiveRepository.connect()?.persist(listener)
            ?: fail("Repository not found!")
        assertEquals(ProgressListenerState.DONE, listener.state)
    }

    @After fun cleanPersistedRepository() {
        check(File(".chronolens").deleteRecursively()) {
            "Couldn't clean up the persisted repository!"
        }
    }
}
