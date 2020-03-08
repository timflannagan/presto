/*
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
package io.prestosql.memory;

import io.airlift.units.DataSize;
import org.testng.annotations.Test;

import static io.airlift.units.DataSize.Unit.GIGABYTE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestLocalMemoryManager
{
    @Test
    public void testReservedMemoryPoolDisabled()
    {
        NodeMemoryConfig config = new NodeMemoryConfig()
                .setHeapHeadroom(new DataSize(10, GIGABYTE))
                .setMaxQueryMemoryPerNode(new DataSize(20, GIGABYTE))
                .setMaxQueryTotalMemoryPerNode(new DataSize(20, GIGABYTE));

        LocalMemoryManager localMemoryManager = new LocalMemoryManager(config, new DataSize(60, GIGABYTE).toBytes());
        assertFalse(localMemoryManager.getReservedPool().isPresent());
        assertEquals(localMemoryManager.getPools().size(), 1);
    }

    @Test
    public void testReservedMemoryPoolEnabled()
    {
        NodeMemoryConfig config = new NodeMemoryConfig()
                .setReservedPoolDisabled(false)
                .setHeapHeadroom(new DataSize(10, GIGABYTE))
                .setMaxQueryMemoryPerNode(new DataSize(20, GIGABYTE))
                .setMaxQueryTotalMemoryPerNode(new DataSize(20, GIGABYTE));

        LocalMemoryManager localMemoryManager = new LocalMemoryManager(config, new DataSize(60, GIGABYTE).toBytes());
        assertTrue(localMemoryManager.getReservedPool().isPresent());
        assertEquals(localMemoryManager.getPools().size(), 2);
    }

    @Test
    public void testMaxQueryMemoryPerNodeBiggerThanQueryTotalMemoryPerNode()
    {
        NodeMemoryConfig config = new NodeMemoryConfig()
                .setHeapHeadroom(new DataSize(10, GIGABYTE))
                .setMaxQueryMemoryPerNode(new DataSize(200, GIGABYTE))
                .setMaxQueryTotalMemoryPerNode(new DataSize(20, GIGABYTE));

        assertThatThrownBy(() -> new LocalMemoryManager(config, new DataSize(60, GIGABYTE).toBytes()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Max query memory per node (query.max-memory-per-node) cannot be greater than" +
                        " the max query total memory per node (query.max-total-memory-per-node)");
    }

    @Test
    public void testNotEnoughAvailableMemory()
    {
        NodeMemoryConfig config = new NodeMemoryConfig()
                .setHeapHeadroom(new DataSize(10, GIGABYTE))
                .setMaxQueryMemoryPerNode(new DataSize(20, GIGABYTE))
                .setMaxQueryTotalMemoryPerNode(new DataSize(20, GIGABYTE));

        // 25 GB heap is not sufficient for 10 GB heap headroom and 20 GB query.max-total-memory-per-node
        assertThatThrownBy(() -> new LocalMemoryManager(config, new DataSize(25, GIGABYTE).toBytes()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("Invalid memory configuration\\. The sum of max total query memory per node .* and heap headroom .*" +
                        "cannot be larger than the available heap memory .*");
    }
}
