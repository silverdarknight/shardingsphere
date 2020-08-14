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

package org.apache.shardingsphere.proxy.backend.privilege.model.tree;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TreeTest {

    private Tree treeNode = new Tree();

    @Test
    public void checkTableTest() {
        treeNode.getRoot().addChild("db");
        treeNode.getRoot().getChild("db").addChild("table");
        assertThat(treeNode.checkPath("db", "table"), is(false));
        treeNode.getRoot().getChild("db").getChild("table").setStar();
        assertThat(treeNode.checkPath("db", "table"), is(true));
        assertThat(treeNode.checkPath("db", "table_false"), is(false));
        treeNode.getRoot().getChild("db").setStar();
        assertThat(treeNode.checkPath("db", "table_false"), is(true));
        treeNode.getRoot().setStar();
        assertThat(treeNode.checkPath("db_false", "test_false"), is(true));
        treeNode = new Tree();
    }

    @Test
    public void checkColTest() {
        treeNode.getRoot().addChild("db");
        treeNode.getRoot().getChild("db").addChild("table");
        treeNode.getRoot().getChild("db").getChild("table").addChild("col");
        treeNode.getRoot().getChild("db").getChild("table").getChild("col").setStar();
        assertThat(treeNode.checkPath("db", "table", "col"), is(true));
        assertThat(treeNode.checkPath("db", "table", "col_false"), is(false));
        treeNode.getRoot().getChild("db").getChild("table").setStar();
        assertThat(treeNode.checkPath("db", "table", "col_false"), is(true));
        assertThat(treeNode.checkPath("db", "table_false", "col_false"), is(false));
        treeNode.getRoot().getChild("db").setStar();
        assertThat(treeNode.checkPath("db", "table_false", "col_false"), is(true));
        assertThat(treeNode.checkPath("db_false", "table_false", "col_false"), is(false));
        treeNode.getRoot().setStar();
        assertThat(treeNode.checkPath("db_false", "test_false", "col_false"), is(true));
        treeNode = new Tree();
    }

    @Test
    public void grantTableTest() {
        treeNode.grantPath("db", "table");
        assertThat(treeNode.checkPath("db", "table"), is(true));
        assertThat(treeNode.checkPath("db", "table_false"), is(false));
        treeNode.grantPath("db", "*");
        assertThat(treeNode.checkPath("db", "table_false"), is(true));
        treeNode.grantPath("*", "*");
        assertThat(treeNode.checkPath("db_false", "table_false"), is(true));
        treeNode = new Tree();
    }

    @Test
    public void grantColTest() {
        treeNode.grantPath("db", "table", "col");
        assertThat(treeNode.checkPath("db", "table", "col"), is(true));
        assertThat(treeNode.checkPath("db", "table", "col_f"), is(false));
        treeNode.grantPath("db", "table", "*");
        assertThat(treeNode.checkPath("db", "table", "col_f"), is(true));
        assertThat(treeNode.checkPath("db", "table_f", "col_f"), is(false));
        treeNode.grantPath("db", "*", "*");
        assertThat(treeNode.checkPath("db", "table_f", "col_f"), is(true));
        assertThat(treeNode.checkPath("db_f", "table_f", "col_f"), is(false));
        treeNode.grantPath("*", "*", "*");
        assertThat(treeNode.checkPath("db_f", "table_f", "col_f"), is(true));
        treeNode = new Tree();
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        cols.add("col2");
        treeNode.grantPath("db", "table", cols);
        assertThat(treeNode.checkPath("db", "table", "col1"), is(true));
        assertThat(treeNode.checkPath("db", "table", "col2"), is(true));
        assertThat(treeNode.checkPath("db", "table", "col3"), is(false));
        treeNode = new Tree();
    }

    @Test
    public void revokeTableTest() {
        treeNode.grantPath("db", "table");
        treeNode.revokePath("db", "table");
        assertThat(treeNode.checkPath("db", "table"), is(false));
        assertThat(treeNode.getRoot().getOffspring().size(), is(0));
        treeNode.grantPath("db", "table");
        treeNode.grantPath("db", "*");
        treeNode.revokePath("db", "*");
        assertThat(treeNode.checkPath("db", "table_f"), is(false));
        assertThat(treeNode.getRoot().getOffspring().size(), is(1));
        assertThat(treeNode.getRoot().getContainsStar(), is(false));
        treeNode.grantPath("*", "*");
        treeNode.revokePath("*", "*");
        assertThat(treeNode.getRoot().getOffspring().size(), is(1));
    }

    @Test
    public void revokeColTest() {
        treeNode.grantPath("db", "table", "col");
        treeNode.revokePath("db", "table", "col");
        assertThat(treeNode.getRoot().getOffspring().size(), is(0));
        treeNode.grantPath("db", "table", "col");
        treeNode.revokePath("db", "table", "*");
        assertThat(treeNode.getRoot().getOffspring().size(), is(0));
    }

    @Test
    public void equalsTest() {
        Tree tree1 = new Tree();
        Tree tree2 = new Tree();
        assertThat(tree1.equals(tree2), is(true));
        tree1.getRoot().addChild("test");
        assertThat(tree1.equals(tree2), is(false));
        tree2.getRoot().addChild("test");
        assertThat(tree1.equals(tree2), is(true));
        tree1.getRoot().getChild("test").addChild("t2");
        tree2.getRoot().getChild("test").addChild("t2");
        assertThat(tree1.equals(tree2), is(true));
        tree1.getRoot().setStar();
        tree2.getRoot().setStar();
        assertThat(tree1.equals(tree2), is(true));
        tree1.getRoot().getChild("test").addChild("t3");
        tree2.getRoot().getChild("test").addChild("t3");
        tree1.getRoot().getChild("test").removeChild("t3");
        tree2.getRoot().getChild("test").removeChild("t3");
        assertThat(tree1.equals(tree2), is(true));
    }
}
