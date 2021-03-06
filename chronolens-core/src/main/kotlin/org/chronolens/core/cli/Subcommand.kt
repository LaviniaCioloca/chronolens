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

package org.chronolens.core.cli

import org.chronolens.core.repository.InteractiveRepository
import org.chronolens.core.repository.PersistentRepository
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.Repository.Companion.isValidPath
import org.chronolens.core.repository.Repository.Companion.isValidRevisionId
import picocli.CommandLine.Command
import java.util.ServiceLoader

/**
 * An abstract subcommand of a main command-line interface executable.
 * Implementations must have the [Command] annotation.
 *
 * Subcommands must have a public no-arg constructor and must supply an entry in
 * the `META-INF/services/org.chronolens.core.cli.Subcommand` configuration
 * file.
 */
abstract class Subcommand : Runnable {
    /** The name of this subcommand, as it should be parsed. */
    abstract val name: String

    /**
     * Returns the interactive repository from the current working directory, or
     * exits if no repository is unambiguously detected.
     */
    protected fun connect(): InteractiveRepository =
        InteractiveRepository.connect() ?: exit("Repository not found!")

    /**
     * Returns the persistent repository from the current working directory, or
     * exits if no persisted repository is found.
     */
    protected fun load(): PersistentRepository =
        PersistentRepository.load() ?: exit("Repository not found!")

    /**
     * Validates the given [path], or exits if it is invalid.
     */
    protected fun validatePath(path: String) {
        if (!isValidPath(path)) exit("Invalid path '$path'!")
    }

    /**
     * Validates the given [revision], or exits if it is invalid or doesn't
     * exist.
     */
    protected fun Repository.validateRevision(revision: String) {
        if (!isValidRevisionId(revision)) exit("Invalid revision '$revision'!")
        val revisionExists = revision in listRevisions()
        if (!revisionExists) exit("Revision '$revision' doesn't exist!")
    }

    companion object {
        /** Returns the list of provided subcommands. */
        @JvmStatic
        fun assembleSubcommands(): List<Subcommand> =
            ServiceLoader.load(Subcommand::class.java).toList()
    }
}
