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

package org.chronolens.coupling

import org.chronolens.coupling.Graph.Node

data class ColoredGraph(val graph: Graph, val colors: Map<String, Int>) {
    init {
        require(graph.nodes.map(Node::label).toSet() == colors.keys)
    }
}

fun Graph.colorNodes(groups: Collection<Set<String>>): ColoredGraph {
    val colors = hashMapOf<String, Int>()
    var color = 0
    for ((label, _) in nodes) {
        colors[label] = color
    }
    for (group in groups) {
        color++
        for (label in group) {
            colors[label] = color
        }
    }
    return ColoredGraph(this, colors)
}
