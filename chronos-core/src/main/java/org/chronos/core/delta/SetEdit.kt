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

/**
 * An atomic change which should be applied to a set of elements.
 *
 * @param T the type of the elements of the edited set
 */
sealed class SetEdit<T> {
    companion object {
        /**
         * Applies the given `edits` on this set and returns the result.
         *
         * @param T the type of the elements of the edited set
         * @param edits the edits which should be applied
         * @return the edited set
         * @throws IllegalStateException if this set has an invalid state and
         * the given `edits` couldn't be applied
        */
        @JvmStatic fun <T> Set<T>.apply(edits: List<SetEdit<T>>): Set<T> =
                edits.fold(toHashSet()) { set, edit ->
                    edit.applyOn(set)
                    set
                }

        /**
         * Returns the edits which should be applied on this set to obtain the
         * `other` set.
         *
         * @param other the set which should be obtained
         * @return the edits which should be applied on this set
         */
        @JvmStatic fun <T> Set<T>.diff(other: Set<T>): List<SetEdit<T>> {
            val added = (other - this).map(::Add)
            val removed = (this - other).map(::Remove)
            return added + removed
        }
    }

    /**
     * Applies this edit on the given mutable set.
     *
     * @param subject the set which should be edited
     * @throws IllegalStateException if the set has an invalid state and this
     * edit couldn't be applied
     */
    protected abstract fun applyOn(subject: MutableSet<T>): Unit

    /**
     * Indicates that an element should be added to the edited set.
     *
     * @param T the type of the elements of the edited set
     * @property value the element which should be added
     */
    data class Add<T>(val value: T) : SetEdit<T>() {
        override fun applyOn(subject: MutableSet<T>) {
            check(subject.add(value))
        }
    }

    /**
     * Indicates that an element should be removed from the edited set.
     *
     * @param T the type of the elements of the edited set
     * @property value the element which should be removed
     */
    data class Remove<T>(val value: T) : SetEdit<T>() {
        override fun applyOn(subject: MutableSet<T>) {
            check(subject.remove(value))
        }
    }
}
