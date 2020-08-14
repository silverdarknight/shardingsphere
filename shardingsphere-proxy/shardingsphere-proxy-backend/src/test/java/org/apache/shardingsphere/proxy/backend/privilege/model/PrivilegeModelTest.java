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

package org.apache.shardingsphere.proxy.backend.privilege.model;

import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeActionType;
import org.apache.shardingsphere.proxy.backend.privilege.model.tree.Tree;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class PrivilegeModelTest {

    private PrivilegeModel testPrivilegeModel;

    @Before
    public void yamlModelTest() throws IOException {
        File privilegeFile = new File(
                ShardingConfigurationLoader.class.getResource("/conf" + "/" + "privilege.yaml")
                        .getFile());
        YamlAccessModel yamlAccessModel = YamlEngine.unmarshal(privilegeFile, YamlAccessModel.class);
        Iterator<Map.Entry<String, YamlPrivilegeConfiguration>> roleIterator = yamlAccessModel
                .getRoleList()
                .entrySet()
                .iterator();
        YamlPrivilegeConfiguration testConfig = roleIterator.next().getValue();
        testPrivilegeModel = new PrivilegeModel();
        testPrivilegeModel.constructModel(testConfig);
        assertThat(testPrivilegeModel.getPrivilegePaths().get(PrivilegeActionType.INSERT).getRoot().hasStar(), is(true));
    }

    @Test
    public void grantTest() {
        // chose privilege type
        Tree tmpTree = testPrivilegeModel.chosePrivilegeType("insert");
        assertThat(tmpTree.getRoot().hasStar(), is(true));
        // grant cols
        List<String> cols = new LinkedList<>();
        cols.add("col");
        testPrivilegeModel.grant("delete", "testDB", "testTable", cols);
        assertThat(testPrivilegeModel.chosePrivilegeType("delete")
                .getRoot()
                .getChild("testDB")
                .getChild("testTable")
                .getOffspring().size(), is(1));
        // grant table
        testPrivilegeModel.grant("delete", "testDB", "testTable2");
        assertThat(testPrivilegeModel.chosePrivilegeType("delete")
                .getRoot()
                .getChild("testDB")
                .getOffspring().size(), is(2));
    }

    @Test
    public void revokeTest() {
        // revoke cols
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        testPrivilegeModel.revoke("select", "testDB", "testTable", cols);
        assertThat(testPrivilegeModel.chosePrivilegeType("select")
                .getRoot().getOffspring().size(), is(0));
        // revoke table
        testPrivilegeModel.revoke("update", "testDB", "testTable");
        assertThat(testPrivilegeModel.chosePrivilegeType("update")
                .getRoot()
                .getChild("testDB")
                .getChild("testTable") == null, is(true));
    }

    @Test
    public void checkTest() {
        assertThat(testPrivilegeModel.checkPrivilege("update",
                "testDB",
                "testTable2",
                "col1"), is(true));
        assertThat(testPrivilegeModel.checkPrivilege("update",
                "testDB",
                "testTable2"), is(false));
    }

    @Test
    public void splitTest() {
        assertThat(Arrays.equals(PrivilegeModel.splitInformation("*.*"), new String[]{"*", "*"}), is(true));
    }
}
