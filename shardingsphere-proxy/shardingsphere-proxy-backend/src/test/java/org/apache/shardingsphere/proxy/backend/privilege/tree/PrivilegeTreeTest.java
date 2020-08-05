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

package org.apache.shardingsphere.proxy.backend.privilege.tree;

import org.apache.shardingsphere.proxy.backend.privilege.tree.PrivilegeTree;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class PrivilegeTreeTest {

    @Test
    public void constructTest() {
        PrivilegeTree tree = new PrivilegeTree();
        assertThat(tree, instanceOf(PrivilegeTree.class));
    }

    @Test
    public void grantTableTest() {
        PrivilegeTree tree = new PrivilegeTree();
        tree.grantPath("db1", "table1");
        tree.grantPath("db1", "table2");
        assertThat(tree.checkPath("db1", "table1"), is(true));
        assertThat(tree.checkPath("db1", "table2"), is(true));
        assertThat(tree.checkPath("db1", "error"), is(false));
        assertThat(tree.checkPath("db2", "table1"), is(false));
        tree.grantPath("db2", "*");
        assertThat(tree.checkPath("db2", "error"), is(true));
        assertThat(tree.checkPath("error", "error"), is(false));
        tree.grantPath("*", "*");
        assertThat(tree.checkPath("error", "error"), is(true));
    }

    @Test
    public void grantColumnTest() {
        PrivilegeTree tree = new PrivilegeTree();
        tree.grantPath("db1", "table1");
        assertThat(tree.checkPath("db1", "table1", "any"), is(true));
        assertThat(tree.checkPath("db1", "table2", "any"), is(false));
        List<String> cols1 = new LinkedList<>();
        cols1.add("col1");
        cols1.add("col2");
        tree.grantPath("db1", "table2", cols1);
        assertThat(tree.checkPath("db1", "table2", "col1"), is(true));
        assertThat(tree.checkPath("db1", "table2", "error"), is(false));
        assertThat(tree.checkPath("db1", "table2", "col2"), is(true));
        assertThat(tree.checkPath("db1", "error", "col1"), is(false));
        assertThat(tree.checkPath("error", "table2", "col1"), is(false));
        cols1.add("*");
        tree.grantPath("db1", "table2", cols1);
        assertThat(tree.checkPath("db1", "table2", "error"), is(true));
        assertThat(tree.checkPath("db1", "error", "col1"), is(false));
    }

    @Test
    public void revokeTableTest() {
        PrivilegeTree tree = new PrivilegeTree();
        tree.grantPath("db1", "table1");
        // no such grant error
        // tree.revokePath("db1", "*");
        tree.revokePath("db1", "table1");
        tree.grantPath("db1", "*");
        // no such grant error
        //tree.revokePath("db1", "table1");
        tree.revokePath("db1", "*");
        tree.grantPath("db1", "table1");
        tree.grantPath("db1", "table2");
        tree.revokePath("db1", "table2");
        assertThat(tree.checkPath("db1", "table1"), is(true));
        assertThat(tree.checkPath("db1", "table2"), is(false));
        tree.grantPath("db1", "*");
        tree.revokePath("db1", "table1");
        assertThat(tree.checkPath("db1", "table1"), is(true));
        assertThat(tree.checkPath("db1", "error"), is(true));
        tree.revokePath("db1", "*");
        assertThat(tree.checkPath("db1", "table1"), is(false));
    }

    @Test
    public void revokeColumnTest() {
        PrivilegeTree tree = new PrivilegeTree();
        List<String> cols1 = new LinkedList<>();
        cols1.add("*");
        tree.grantPath("db1", "table1", cols1);
        List<String> testCols = new LinkedList<>();
        // no such grant error
        // testCols.add("col1");
        // tree.revokePath("db1", "table1", testCols);
        testCols = new LinkedList<>();
        testCols.add("*");
        tree.revokePath("db1", "table1", testCols);
        testCols = new LinkedList<>();
        testCols.add("col1");
        testCols.add("col2");
        tree.grantPath("db1", "table1", testCols);
        testCols = new LinkedList<>();
        testCols.add("col2");
        tree.revokePath("db1", "table1", testCols);
        assertThat(tree.checkPath("db1", "table1", "col1"), is(true));
        assertThat(tree.checkPath("db1", "table1", "col2"), is(false));
        testCols = new LinkedList<>();
        testCols.add("*");
        tree.revokePath("db1", "table1", testCols);
        assertThat(tree.checkPath("db1", "table1", "col1"), is(false));
    }
}
