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

import org.chronos.core.Node
import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable

import kotlin.reflect.KClass

sealed class NodeChange {
    data class Add(val node: Node) : NodeChange()

    /*data class AddType(val type: Type) : NodeChange()

    data class AddVariable(val variable: Variable) : NodeChange()

    data class AddFunction(val function: Function) : NodeChange()*/

    data class Remove(
            val type: KClass<out Node>,
            val key: String
    ) : NodeChange() {
        companion object {
            @JvmStatic
            inline operator fun <reified T : Node> invoke(key: String): Remove =
                    Remove(T::class, key)
        }
    }

    /*data class RemoveType(val name: String) : NodeChange()

    data class RemoveVariable(val name: String) : NodeChange()

    data class RemoveFunction(val signature: String) : NodeChange()*/

    data class ChangeNode<T : Node>(
            val type: KClass<T>,
            val key: String,
            val change: Change<T>
    ) : NodeChange() {
        companion object {
            @JvmStatic
            inline operator fun <reified T : Node> invoke(
                    key: String,
                    change: Change<T>
            ): ChangeNode<T> = ChangeNode(T::class, key, change)
        }
    }

    /*data class ChangeType(
            val name: String,
            val typeChange: TypeChange
    ) : NodeChange()

    data class ChangeVariable(
            val name: String,
            val variableChange: VariableChange
    ) : NodeChange()

    data class ChangeFunction(
            val signature: String,
            val functionChange: FunctionChange
    ) : NodeChange()*/
}
