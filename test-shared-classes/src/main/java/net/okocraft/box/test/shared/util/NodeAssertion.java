/*
 *     Copyright 2024 Siroshun09
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.okocraft.box.test.shared.util;

import dev.siroshun.configapi.core.node.ListNode;
import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.core.node.Node;
import dev.siroshun.configapi.core.node.NumberValue;
import dev.siroshun.configapi.core.node.ValueNode;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

/**
 * A utility class for asserting {@link Node}s.
 */
public final class NodeAssertion {

    /**
     * Asserts that {@code a} and {@code b} {@link Node}s are equal.
     *
     * @param a a first {@link Node}
     * @param b a second {@link Node}
     */
    public static void assertEquals(@NotNull Node<?> a, @NotNull Node<?> b) {
        if (a instanceof ListNode listNodeA && b instanceof ListNode listNodeB) {
            assertEquals(listNodeA, listNodeB);
        } else if (a instanceof MapNode mapNodeA && b instanceof MapNode mapNodeB) {
            assertEquals(mapNodeA, mapNodeB);
        } else if (a instanceof NumberValue numberA && b instanceof NumberValue numberB) {
            if (numberA.compareTo(numberB) != 0) {
                fail(a, b);
            }
        } else if (a instanceof ValueNode<?> valueA && b instanceof ValueNode<?> valueB) {
            if (!a.value().equals(b.value())) {
                fail(a, b);
            }
        } else {
            if (!a.equals(b)) {
                fail(a, b);
            }
        }
    }

    /**
     * Asserts that {@code a} and {@code b} {@link ListNode}s are equal.
     *
     * @param a a first {@link ListNode}
     * @param b a second {@link ListNode}
     */
    public static void assertEquals(@NotNull ListNode a, @NotNull ListNode b) {
        List<Node<?>> listA = a.value();
        List<Node<?>> listB = b.value();

        if (listA.size() != listB.size()) {
            fail(a, b);
        }

        for (int i = 0, mapASize = listA.size(); i < mapASize; i++) {
            assertEquals(listA.get(i), listB.get(i));
        }

        Assertions.assertEquals(a.getCommentOrNull(), b.getCommentOrNull());
    }

    /**
     * Asserts that {@code a} and {@code b} {@link MapNode}s are equal.
     *
     * @param a a first {@link MapNode}
     * @param b a second {@link MapNode}
     */
    public static void assertEquals(@NotNull MapNode a, @NotNull MapNode b) {
        Map<Object, Node<?>> mapA = a.value();
        Map<Object, Node<?>> mapB = b.value();

        if (mapA.size() != mapB.size()) {
            fail(a, b);
        }

        for (Object key : mapA.keySet()) {
            if (!mapB.containsKey(key)) {
                fail(a, b);
            }

            Node<?> nodeA = mapA.get(key);
            Node<?> nodeB = mapB.get(key);

            assertEquals(nodeA, nodeB);
        }

        Assertions.assertEquals(a.getCommentOrNull(), b.getCommentOrNull());
    }

    private static void fail(@NotNull Node<?> a, @NotNull Node<?> b) {
        AssertionFailureBuilder.assertionFailure().expected(a).actual(b).buildAndThrow();
    }

    private NodeAssertion() {
        throw new UnsupportedOperationException();
    }
}
