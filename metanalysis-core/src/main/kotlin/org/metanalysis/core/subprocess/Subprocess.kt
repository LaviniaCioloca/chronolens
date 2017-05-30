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

package org.metanalysis.core.subprocess

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.InterruptedIOException

object Subprocess {
    private fun InputStream.readText(): String =
            reader().use(InputStreamReader::readText)

    /**
     * Executes the given `command` in a subprocess and returns its result.
     *
     * The `command` output to `stdout` and `stderr` must be `UTF-8` encoded
     * text.
     *
     * @param command the command which should be executed
     * @return the parsed input from `stdout` (if the subprocess terminated
     * normally) or from `stderr` (if the subprocess terminated abnormally)
     * @throws InterruptedIOException if the current thread is interrupted
     * while waiting for the subprocess to terminate
     * @throws IOException if any input related errors occur
     */
    @Throws(IOException::class)
    @JvmStatic fun execute(vararg command: String): Result {
        val process = ProcessBuilder().command(*command).start()
        try {
            process.outputStream.close()
            val input = process.inputStream.readText()
            val error = process.errorStream.readText()
            val exitCode = process.waitFor()
            return if (exitCode == 0) Result.Success(input)
            else Result.Error(exitCode, error)
        } catch (e: InterruptedException) {
            throw InterruptedIOException(e.message)
        } finally {
            process.destroy()
        }
    }
}
