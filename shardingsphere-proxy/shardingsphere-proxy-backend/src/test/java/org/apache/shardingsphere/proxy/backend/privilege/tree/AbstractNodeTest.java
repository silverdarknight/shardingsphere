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

public class AbstractNodeTest {
    private PrivilegeTableNode abstractNode = new PrivilegeTableNode("testTable");

    @Test
    public void isPathTest() {
        abstractNode.setIsRegNode(true);
        // assertThat(abstractNode.isPath("test"),is(true));
        abstractNode.setIsRegNode(false);
        assertThat(abstractNode.isPath("testTable"), is(true));
        assertThat(abstractNode.isPath("testTable_false"), is(false));
    }

    @Test
    public void likePathTest() {
        abstractNode.setStar();
        assertThat(abstractNode.getContainsStar(), is(true));
    }

    @Test
    public void contentIsRegTest() {
        assertThat(abstractNode.contentIsReg("col"), is(false));
    }

    @Test
    public void containsOffspringTest() {
        abstractNode = new PrivilegeTableNode("testTable");
        assertThat(abstractNode.containsOffspring(), is(false));
        abstractNode.setStar();
        assertThat(abstractNode.containsOffspring(), is(true));
        abstractNode = new PrivilegeTableNode("testTable");
        abstractNode.addChild("test");
        assertThat(abstractNode.containsOffspring(), is(true));
    }

    @Test
    public void clearEmptyPathsTest() {
        assertThat(abstractNode.clearEmptyPaths(), is(true));
        abstractNode.addChild("col");
        assertThat(abstractNode.clearEmptyPaths(), is(false));
    }

    @Test
    public void getChildTest() {
        assertThat(abstractNode.getChild("test") == null, is(true));
        abstractNode.addChild("col");
        assertThat(abstractNode.getChild("col").getContent(), is("col"));
        abstractNode = new PrivilegeTableNode("testTable");
    }

    @Test
    public void containsNodeTest() {
        assertThat(abstractNode.containsNode("col"), is(false));
        abstractNode.setStar();
        assertThat(abstractNode.containsNode("*"), is(true));
        abstractNode.addChild("col");
        assertThat(abstractNode.containsNode("col"), is(true));
    }
}
