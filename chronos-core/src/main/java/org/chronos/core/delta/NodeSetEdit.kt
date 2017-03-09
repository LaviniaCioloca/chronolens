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
import org.chronos.core.delta.Transaction.Companion.apply
import org.chronos.core.delta.FunctionTransaction.Companion.diff
import org.chronos.core.delta.TypeTransaction.Companion.diff
import org.chronos.core.delta.VariableTransaction.Companion.diff

import kotlin.reflect.KClass

/** An atomic change which should be applied to a set of nodes. */
sealed class NodeSetEdit {
    companion object {
        private val Node.key: NodeKey
            get() = NodeKey(this::class, identifier)

        /**
         * Applies the given list of `edits` on this set of nodes and returns
         * the result.
         *
         * @param edits the edits which should be applied
         * @return the edited set of nodes
         * @throws IllegalStateException if this set of nodes has an invalid
         * state and the given `edits` couldn't be applied
         */
        @JvmStatic fun Set<Node>.apply(edits: List<NodeSetEdit>): Set<Node> {
            val map = associateBy { it.key }.toMutableMap()
            return edits.fold(map) { nodes, edit ->
                edit.applyOn(nodes)
                nodes
            }.values.toSet()
        }

        /**
         * Returns the edits which should be applied on this set of nodes to
         * obtain the `other` set of nodes.
         *
         * @param other the set of nodes which should be obtained
         * @return the edits which should be applied on this set of nodes
         */
        @JvmStatic fun Set<Node>.diff(other: Set<Node>): List<NodeSetEdit> {
            val map = other.associateBy { it.key }.toMutableMap()
            val added = (other - this).map(::Add)
            val removed = (this - other).map { node ->
                Remove(node::class, node.identifier)
            }
            val changed = this.intersect(other).map { node ->
                val identifier = node.identifier
                val otherNode = checkNotNull(map[node.key])
                when (node) {
                    is Type -> node.diff(otherNode as Type)?.let { t ->
                        Change<Type>(identifier, t)
                    }
                    is Variable -> node.diff(otherNode as Variable)?.let { t ->
                        Change<Variable>(identifier, t)
                    }
                    is Function -> node.diff(otherNode as Function)?.let { t ->
                        Change<Function>(identifier, t)
                    }
                }
            }.filterNotNull()
            return added + removed + changed
        }
    }

    protected data class NodeKey(
            val nodeType: KClass<out Node>,
            val identifier: String
    )

    protected abstract val key: NodeKey
    protected abstract fun applyOn(subject: MutableMap<NodeKey, Node>): Unit

    /**
     * Indicates that a node should be added to the edited set of nodes.
     *
     * @property node the added node
     */
    data class Add(val node: Node) : NodeSetEdit() {
        override val key: NodeKey
            get() = NodeKey(node::class, node.identifier)

        override fun applyOn(subject: MutableMap<NodeKey, Node>) {
            check(subject.put(key, node) == null)
        }
    }

    /**
     * Indicates that a node should be removed from the edited set of nodes.
     *
     * @property nodeType the class object of the removed node
     * @property identifier the `identifier` of the removed node
     */
    data class Remove(
            val nodeType: KClass<out Node>,
            val identifier: String
    ) : NodeSetEdit() {
        companion object {
            /** Utility factory method. */
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String
            ): Remove = Remove(T::class, identifier)
        }

        override val key: NodeKey
            get() = NodeKey(nodeType, identifier)

        override fun applyOn(subject: MutableMap<NodeKey, Node>) {
            checkNotNull(subject.remove(key))
        }
    }

    /**
     * Indicates that a node should be change in the edited set of nodes.
     *
     * @param T the type of the changed node
     * @property nodeType the class object of the changed node subtype
     * @property identifier the `identifier` of the changed node
     * @property transaction the transaction which should be applied on the
     * changed node
     */
    data class Change<T : Node>(
            val nodeType: KClass<T>,
            val identifier: String,
            val transaction: Transaction<T>
    ) : NodeSetEdit() {
        companion object {
            /** Utility factory method. */
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String,
                    transaction: Transaction<T>
            ): Change<T> = Change(T::class, identifier, transaction)
        }

        override val key: NodeKey
            get() = NodeKey(nodeType, identifier)

        override fun applyOn(subject: MutableMap<NodeKey, Node>) {
            subject[key] = checkNotNull(nodeType.java.cast(subject[key]))
                    .apply(transaction)
        }
    }
}
