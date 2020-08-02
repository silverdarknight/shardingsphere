/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

public class RolePrivilegeTest {

    @Test
    public void assertRolePrivilegeGenerator() {
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        assertThat(rolePrivilege, instanceOf(RolePrivilege.class));
    }

    @Test
    public void assertRolePrivilegeCheckExecutor() {
        RolePrivilege rolePrivilege = new RolePrivilege("testRole.*");
        rolePrivilege.grant("select", "testDB1.*");
        rolePrivilege.grant("select", "testDB2.testTable");
        LinkedList<String> cols = new LinkedList<>();
        cols.add("col1");
        rolePrivilege.grant("select", "testDB3.testTable", cols);
        // information (database / information)
        assertThat(rolePrivilege.checkPrivilege("select", "testDB_false.*"), is(false));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB1.*"), is(true));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB2.*"), is(false));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB2.testTable"), is(true));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB2. testTable"), is(true));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB3.testTable"), is(false));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB3.testTable.col1"), is(true));
        // database table
        assertThat(rolePrivilege.checkPrivilege("select", "testDB1", "testTable"), is(true));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB2", " testTable"), is(true));
        // database table col
        assertThat(rolePrivilege.checkPrivilege("select", "testDB1", "testTable", "col1"), is(true));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB1", "testTable", "col1 "), is(true));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB_false", "testTable", "col1"), is(false));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB", "testTable_false", "col1"), is(false));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB", "testTable", "col1_false"), is(false));
    }

    @Test
    public void assertRolePrivilegeGrantExecutor() {
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        RolePrivilege rolePrivilege2 = new RolePrivilege("testRole2");
        // information (database / information)
        rolePrivilege.grant("select", "*.*");
        assertThat(rolePrivilege.checkPrivilege("select", "testDB.*"), is(true));
        rolePrivilege2.grant("select", "testDB.*");
        assertThat(rolePrivilege2.checkPrivilege("select", "testDB.*"), is(true));
        assertThat(rolePrivilege2.checkPrivilege("select", "testDB_false.*"), is(false));
        rolePrivilege2.grant("select", "testDB2.testTable");
        assertThat(rolePrivilege2.checkPrivilege("select", "testDB2.testTable"), is(true));
        assertThat(rolePrivilege2.checkPrivilege("select", "testDB2.testTable_false"), is(false));
        // database table
        rolePrivilege.grant("delete", "testDB", "testTable");
        assertThat(rolePrivilege.checkPrivilege("delete", "testDB", "testTable"), is(true));
        assertThat(rolePrivilege.checkPrivilege("delete", "testDB", "testTable_false"), is(false));
        // database table column
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        rolePrivilege.grant("update", "testDB", "testTable", cols);
        assertThat(rolePrivilege.checkPrivilege("update", "testDB", "testTable", "col1"), is(true));
        assertThat(rolePrivilege.checkPrivilege("update", "testDB", "testTable", "col_false"), is(false));
    }

    @Test
    public void assertRolePrivilegeRevokeExecutor() {
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        rolePrivilege.grant("select", "testDB2.*");
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        rolePrivilege.grant("select", "testDB.testTable", cols);
        rolePrivilege.grant("select", "testDB.testTable2");
        cols = new LinkedList<>();
        cols.add("col3");
        rolePrivilege.grant("select", "testDB.testTable", cols);
        // information (database / information)
        cols = new LinkedList<>();
        cols.add("col1");
        rolePrivilege.revoke("select", "testDB.testTable", cols);
        rolePrivilege.revoke("select", "testDB2.*");
        assertThat(rolePrivilege.checkPrivilege("select", "testDB2.*"), is(false));
        assertThat(rolePrivilege.checkPrivilege("select", "testDB.testTable.col1"), is(false));
        // database table
        rolePrivilege.revoke("select", "testDB", "testTable2");
        assertThat(rolePrivilege.checkPrivilege("select", "testDB.testTable2"), is(false));
        // database table column
        cols = new LinkedList<>();
        cols.add("col3");
        rolePrivilege.revoke("select", "testDB", "testTable", cols);
        assertThat(rolePrivilege.checkPrivilege("select", "testDB.testTable.col3"), is(false));
    }
}
