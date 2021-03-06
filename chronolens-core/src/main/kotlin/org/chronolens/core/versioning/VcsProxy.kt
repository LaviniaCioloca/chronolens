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

package org.chronolens.core.versioning

/**
 * A version control system (VCS) proxy which interacts with the repository
 * detected in the current working directory.
 *
 * The associated VCS must be supported in the current environment and the
 * detected repository must be in a valid state.
 */
interface VcsProxy {
    /** Returns the `head` revision. */
    fun getHead(): Revision

    /**
     * Returns the revision with the given [revisionId], or `null` if no such
     * revision exists.
     */
    fun getRevision(revisionId: String): Revision?

    /**
     * Returns the files inside the current working directory which were
     * modified in the revision with the given [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     */
    fun getChangeSet(revisionId: String): Set<String>

    /**
     * Returns the set of existing files in the current working directory in the
     * revision with the given [revisionId].
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     */
    fun listFiles(revisionId: String): Set<String>

    /**
     * Returns the content of the file located at the given relative [path] as
     * it is found in the revision with the given [revisionId], or `null` if it
     * doesn't exist in the specified revision.
     *
     * @throws IllegalArgumentException if [revisionId] doesn't exist
     */
    fun getFile(revisionId: String, path: String): String?

    /**
     * Returns the chronological list of revisions which modified the file or
     * directory at the given [path] up to the `head` revision, or the empty
     * list if [path] never existed in the `head` revision or any of its
     * ancestors.
     *
     * A directory is modified if any file in its subtree is modified. The empty
     * string is a path that represents the current working directory.
     */
    fun getHistory(path: String = ""): List<Revision>
}
