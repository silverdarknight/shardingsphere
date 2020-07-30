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
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class TableNodeTest {

    private PrivilegeTableNode tableNode = new PrivilegeTableNode("table");

    @Test
    public void addTest(){
        assertThat(tableNode.addChild("*"),is(true));
        assertThat(tableNode.addChild("*"),is(false));
        assertThat(tableNode.addChild("col"),is(true));
        assertThat(tableNode.addChild("col"),is(false));
    }

    @Test
    public void removeTest(){
        tableNode.addChild("*");
        tableNode.addChild("col");
        assertThat(tableNode.removeChild("col"),is(true));
        assertThat(tableNode.removeChild("col"),is(false));
        assertThat(tableNode.removeChild("*"),is(true));
        assertThat(tableNode.removeChild("*"),is(false));
    }

    @Test
    public void containsTest(){
        tableNode.addChild("col");
        assertThat(tableNode.containsChild("col"),is(true));
        assertThat(tableNode.containsChild("col_false"),is(false));
        tableNode.addChild("*");
        assertThat(tableNode.containsChild("col_false"),is(true));
    }
}
