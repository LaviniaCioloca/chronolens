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

package org.chronolens.git

import org.chronolens.core.subprocess.Subprocess.execute
import org.chronolens.core.versioning.VcsProxy
import org.chronolens.core.versioning.VcsProxyFactory

/** Creates proxies which delegate their operations to the `git` VCS. */
class GitProxyFactory : VcsProxyFactory() {
    private val vcs: String = "git"

    private fun getPrefix(): String? {
        val result = execute(vcs, "rev-parse", "--show-prefix")
        val rawPrefix = result.getOrNull() ?: return null
        return rawPrefix.lines().first()
    }

    override fun isSupported(): Boolean = execute(vcs, "--version").isSuccess

    override fun createProxy(): VcsProxy? {
        val prefix = getPrefix() ?: return null
        return GitProxy(prefix)
    }
}
