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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TreeTest {

    private PrivilegeTree treeNode = new PrivilegeTree();

    @Test
    public void CheckTableTest(){
        treeNode.root.addChild("db");
        treeNode.root.getChild("db").addChild("table");
        assertThat(treeNode.checkPath("db","table"), is(false));
        treeNode.root.getChild("db").getChild("table").setStar();
        assertThat(treeNode.checkPath("db","table"), is(true));
        assertThat(treeNode.checkPath("db","table_false"), is(false));
        treeNode.root.getChild("db").setStar();
        assertThat(treeNode.checkPath("db","table_false"), is(true));
        treeNode.root.setStar();
        assertThat(treeNode.checkPath("db_false","test_false"), is(true));
    }

    @Test
    public void CheckColTest(){
        treeNode.root.addChild("db");
        treeNode.root.getChild("db").addChild("table");
        treeNode.root.getChild("db").getChild("table").addChild("col");
        assertThat(treeNode.checkPath("db","table","col"), is(true));
        assertThat(treeNode.checkPath("db","table","col_false"), is(false));
        treeNode.root.getChild("db").getChild("table").setStar();
        assertThat(treeNode.checkPath("db","table","col_false"), is(true));
        assertThat(treeNode.checkPath("db","table_false","col_false"), is(false));
        treeNode.root.getChild("db").setStar();
        assertThat(treeNode.checkPath("db","table_false","col_false"), is(true));
        assertThat(treeNode.checkPath("db_false","table_false","col_false"), is(false));
        treeNode.root.setStar();
        assertThat(treeNode.checkPath("db_false","test_false","col_false"), is(true));
    }
}
