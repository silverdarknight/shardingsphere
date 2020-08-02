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

public class RootNodeTest {

    private PrivilegeRootNode rootNode = new PrivilegeRootNode("table");

    @Test
    public void addTest() {
        assertThat(rootNode.addChild("*"), is(true));
        assertThat(rootNode.addChild("*"), is(false));
        assertThat(rootNode.addChild("col"), is(true));
        assertThat(rootNode.addChild("col"), is(false));
    }

    @Test
    public void removeTest() {
        rootNode.addChild("*");
        rootNode.addChild("col");
        assertThat(rootNode.removeChild("col"), is(true));
        assertThat(rootNode.removeChild("col"), is(false));
        assertThat(rootNode.removeChild("*"), is(true));
        assertThat(rootNode.removeChild("*"), is(false));
    }

    @Test
    public void containsTest() {
        rootNode.addChild("col");
        assertThat(rootNode.containsChild("col"), is(true));
        assertThat(rootNode.containsChild("col_false"), is(false));
        rootNode.addChild("*");
        assertThat(rootNode.containsChild("col_false"), is(true));
    }
}
