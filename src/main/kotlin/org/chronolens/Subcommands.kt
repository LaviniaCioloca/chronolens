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

package org.chronolens

import org.chronolens.core.cli.Subcommand
import org.chronolens.core.cli.exit
import org.chronolens.core.model.sourcePath
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.PersistentRepository
import org.chronolens.core.repository.PersistentRepository.Companion.persist
import org.chronolens.core.repository.PersistentRepository.ProgressListener
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "ls-tree",
    description = [
        "Prints all the interpretable files of the repository from the "
            + "specified revision."
    ]
)
class ListTree : Subcommand() {
    override val name: String get() = "ls-tree"
    private val repository by lazy(::connect)

    @Option(
        names = ["--rev"],
        description = ["the inspected revision (default: the <head> revision)"]
    )
    private var revisionId: String? = null
    private val revision: String get() = revisionId ?: repository.getHeadId()

    override fun run() {
        repository.validateRevision(revision)
        repository.listSources(revision).forEach(::println)
    }
}

@Command(
    name = "rev-list",
    description = [
        "Prints all revisions on the path from the currently checked-out "
            + "(<head>) revision to the root of the revision tree / graph in "
            + "chronological order."
    ]
)
class RevList : Subcommand() {
    override val name: String get() = "rev-list"

    override fun run() {
        val repository = connect()
        repository.listRevisions().forEach(::println)
    }
}

@Command(
    name = "model",
    description = [
        "Prints the interpreted model of the source node with the specified id "
            + "as it is found in the given revision of the repository.",
        "The path separator is '/', types are separated by ':' and functions "
            + "and variables are separated by '#'."
    ]
)
class Model : Subcommand() {
    override val name: String get() = "model"
    private val repository by lazy(::connect)

    @Option(
        names = ["--id"],
        description = ["the inspected source node"],
        required = true
    )
    private lateinit var id: String

    @Option(
        names = ["--rev"],
        description = ["the inspected revision (default: the <head> revision)"]
    )
    private var revisionId: String? = null
    private val revision: String get() = revisionId ?: repository.getHeadId()

    override fun run() {
        val path = id.sourcePath
        validatePath(path)
        repository.validateRevision(revision)
        val model = repository.getSource(path, revision)
            ?: exit("File '$path' couldn't be interpreted or doesn't exist!")
        val node = model.walkSourceTree().find { it.id == id }
            ?: exit("Source node '$id' doesn't exist!")
        PrettyPrinterVisitor(System.out).visit(node)
    }
}

@Command(
    name = "persist",
    description = [
        "Connects to the repository and persists the source and history model "
            + "from all the files that can be interpreted.",
        "The model is persisted in the '.chronolens' directory from the "
            + "current working directory."
    ]
)
class Persist : Subcommand() {
    override val name: String get() = "persist"

    override fun run() {
        val repository = connect()
        repository.persist(object : ProgressListener {
            private var sources = 0
            private var transactions = 0
            private var i = 0

            override fun onSnapshotStart(headId: String, sourceCount: Int) {
                println("Persisting snapshot '$headId'...")
                sources = sourceCount
                i = 0
            }

            override fun onSourcePersisted(path: String) {
                i++
                print("Persisted $i / $sources sources...\r")
            }

            override fun onSnapshotEnd() {
                println()
                println("Done!")
            }

            override fun onHistoryStart(revisionCount: Int) {
                println("Persisting transactions...")
                transactions = revisionCount
                i = 0
            }

            override fun onTransactionPersisted(id: String) {
                i++
                print("Persisted $i / $transactions transactions...\r")
            }

            override fun onHistoryEnd() {
                println()
                println("Done!")
            }
        })
    }
}

@Command(
    name = "clean",
    description = [
        "Deletes the previously persisted repository from the current working "
            + "directory, if it exists."
    ]
)
class Clean : Subcommand() {
    override val name: String get() = "clean"

    override fun run() {
        PersistentRepository.clean()
    }
}
