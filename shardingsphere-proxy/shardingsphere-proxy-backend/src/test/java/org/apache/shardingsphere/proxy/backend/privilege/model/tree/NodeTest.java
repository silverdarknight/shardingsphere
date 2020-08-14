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
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class NodeTest {

    private Node node = new Node("testNode");

    @Test
    public void starTest() {
        assertThat(node.hasStar(), is(false));
        node.setStar();
        assertThat(node.hasStar(), is(true));
    }

    @Test
    public void containsOffspringTest() {
        node = new Node("testTable");
        assertThat(node.containsOffspring(), is(false));
        node.setStar();
        assertThat(node.containsOffspring(), is(true));
        node = new Node("testTable");
        node.addChild("test");
        assertThat(node.containsOffspring(), is(true));
    }

    @Test
    public void clearEmptyPathsTest() {
        assertThat(node.clearEmptyPaths(), is(true));
        node.addChild("col");
        node.getChild("col").setStar();
        assertThat(node.clearEmptyPaths(), is(false));
    }

    @Test
    public void getChildTest() {
        assertThat(node.getChild("test") == null, is(true));
        node.addChild("col");
        assertThat(node.getChild("col").getContent(), is("col"));
        node = new Node("testTable");
    }

    @Test
    public void containsChildTest() {
        assertThat(node.containsChild("col"), is(false));
        node.setStar();
        assertThat(node.containsChild("*"), is(true));
        node.addChild("col");
        assertThat(node.containsChild("col"), is(true));
    }

    @Test
    public void removeTest() {
        node.addChild("*");
        node.addChild("col");
        assertThat(node.removeChild("col"), is(true));
        assertThat(node.removeChild("col"), is(false));
        assertThat(node.removeChild("*"), is(true));
        assertThat(node.removeChild("*"), is(false));
    }

    @Test
    public void addTest() {
        assertThat(node.addChild("*"), is(true));
        assertThat(node.addChild("*"), is(false));
        assertThat(node.addChild("col"), is(true));
        assertThat(node.addChild("col"), is(false));
    }

    @Test
    public void equalsTest() {
        Node n1 = new Node("1");
        Node n2 = new Node("1");
        assertThat(n1.equals(n2), is(true));
        n1.setStar();
        n2.setStar();
        assertThat(n1.equals(n2), is(true));
        n1.addChild("child");
        assertThat(n1.equals(n2), is(false));
        n2.addChild("child");
        assertThat(n1.equals(n2), is(true));
    }
}
