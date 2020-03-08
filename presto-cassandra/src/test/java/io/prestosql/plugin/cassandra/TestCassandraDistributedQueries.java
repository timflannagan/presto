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
package io.prestosql.plugin.cassandra;

import io.airlift.tpch.TpchTable;
import io.prestosql.testing.AbstractTestDistributedQueries;
import io.prestosql.testing.MaterializedResult;
import io.prestosql.testing.QueryRunner;
import org.testng.annotations.AfterClass;

import static io.prestosql.plugin.cassandra.CassandraQueryRunner.createCassandraQueryRunner;
import static io.prestosql.spi.type.VarcharType.VARCHAR;
import static io.prestosql.testing.MaterializedResult.resultBuilder;
import static io.prestosql.testing.assertions.Assert.assertEquals;

public class TestCassandraDistributedQueries
        extends AbstractTestDistributedQueries
{
    private CassandraServer server;

    @Override
    protected QueryRunner createQueryRunner()
            throws Exception
    {
        this.server = new CassandraServer();
        return createCassandraQueryRunner(server, TpchTable.getTables());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown()
    {
        server.close();
    }

    @Override
    protected boolean supportsViews()
    {
        return false;
    }

    @Override
    public void testRenameTable()
    {
        // Cassandra does not support renaming tables
    }

    @Override
    public void testAddColumn()
    {
        // Cassandra does not support adding columns
    }

    @Override
    public void testRenameColumn()
    {
        // Cassandra does not support renaming columns
    }

    @Override
    public void testDropColumn()
    {
        // Cassandra does not support dropping columns
    }

    @Override
    public void testInsert()
    {
        // Cassandra connector currently does not support create table
        // TODO test inserts
    }

    @Override
    public void testInsertWithCoercion()
    {
        // Cassandra connector currently does not support create table
        // TODO test inserts
    }

    @Override
    public void testInsertUnicode()
    {
        // Cassandra connector currently does not support create table
        // TODO test inserts
    }

    @Override
    public void testInsertArray()
    {
        // Cassandra connector currently does not support create table
        // TODO test inserts
    }

    @Override
    public void testCreateTable()
    {
        // Cassandra connector currently does not support create table
    }

    @Override
    public void testDelete()
    {
        // Cassandra connector currently does not support delete
    }

    @Override
    public void testShowColumns()
    {
        MaterializedResult actual = computeActual("SHOW COLUMNS FROM orders");

        MaterializedResult expectedParametrizedVarchar = resultBuilder(getSession(), VARCHAR, VARCHAR, VARCHAR, VARCHAR)
                .row("orderkey", "bigint", "", "")
                .row("custkey", "bigint", "", "")
                .row("orderstatus", "varchar", "", "")
                .row("totalprice", "double", "", "")
                .row("orderdate", "varchar", "", "")
                .row("orderpriority", "varchar", "", "")
                .row("clerk", "varchar", "", "")
                .row("shippriority", "integer", "", "")
                .row("comment", "varchar", "", "")
                .build();

        assertEquals(actual, expectedParametrizedVarchar);
    }

    @Override
    public void testDescribeOutput()
    {
        // this connector uses a non-canonical type for varchar columns in tpch
    }

    @Override
    public void testDescribeOutputNamedAndUnnamed()
    {
        // this connector uses a non-canonical type for varchar columns in tpch
    }

    @Override
    public void testWrittenStats()
    {
        // TODO Cassandra connector supports CTAS and inserts, but the test would fail
    }

    @Override
    public void testCommentTable()
    {
        // Cassandra connector currently does not support comment on table
        assertQueryFails("COMMENT ON TABLE orders IS 'hello'", "This connector does not support setting table comments");
    }
}
