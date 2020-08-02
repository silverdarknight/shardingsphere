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

public class DBNodeTest {

    private PrivilegeDBNode dbNode = new PrivilegeDBNode("table");

    @Test
    public void addTest() {
        assertThat(dbNode.addChild("*"), is(true));
        assertThat(dbNode.addChild("*"), is(false));
        assertThat(dbNode.addChild("col"), is(true));
        assertThat(dbNode.addChild("col"), is(false));
    }

    @Test
    public void removeTest() {
        dbNode.addChild("*");
        dbNode.addChild("col");
        assertThat(dbNode.removeChild("col"), is(true));
        assertThat(dbNode.removeChild("col"), is(false));
        assertThat(dbNode.removeChild("*"), is(true));
        assertThat(dbNode.removeChild("*"), is(false));
    }

    @Test
    public void containsTest() {
        dbNode.addChild("col");
        assertThat(dbNode.containsChild("col"), is(true));
        assertThat(dbNode.containsChild("col_false"), is(false));
        dbNode.addChild("*");
        assertThat(dbNode.containsChild("col_false"), is(true));
    }
}
