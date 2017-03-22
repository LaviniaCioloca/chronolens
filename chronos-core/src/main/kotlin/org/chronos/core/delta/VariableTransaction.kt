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

package org.chronos.core.delta

import org.chronos.core.Node.Variable
import org.chronos.core.delta.MapEdit.Companion.apply
import org.chronos.core.delta.MapEdit.Companion.diff

/**
 * A transaction which should be applied to a [Variable].
 *
 * @property initializerEdit the new initializer of the variable
 * @property propertyEdits the edits which should be applied to the `properties`
 */
data class VariableTransaction(
        val initializerEdit: String?,
        val propertyEdits: List<MapEdit<String, String>> = emptyList()
) : Transaction<Variable> {
    companion object {
        /**
         * Returns the transaction which should be applied on this variable to
         * obtain the `other` variable, or `null` if they are identical.
         *
         * @param other the variable which should be obtained
         * @return the transaction which should be applied on this variable
         * @throws IllegalArgumentException if the given variables have
         * different identifiers
         */
        @JvmStatic fun Variable.diff(other: Variable): VariableTransaction? {
            require(identifier == other.identifier)
            val propertyEdits = properties.diff(other.properties)
            val isChanged = initializer != other.initializer
                    || propertyEdits.isNotEmpty()
            return if (isChanged)
                VariableTransaction(other.initializer, propertyEdits)
            else null
        }
    }

    override fun applyOn(subject: Variable): Variable = subject.copy(
            initializer = initializerEdit,
            properties = subject.properties.apply(propertyEdits)
    )
}