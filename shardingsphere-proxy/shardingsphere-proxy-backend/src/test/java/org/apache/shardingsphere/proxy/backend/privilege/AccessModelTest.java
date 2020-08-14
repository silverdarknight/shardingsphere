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

import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.backend.privilege.model.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserPrivilege;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class AccessModelTest {

    private YamlAccessModel yamlAccessModel;

    private AccessModel accessModel;

    @Before
    public void constructYamlModel() throws IOException {
        File privilegeFile = new File(
                ShardingConfigurationLoader.class.getResource("/conf" + "/" + "privilege.yaml")
                        .getFile());
        yamlAccessModel = YamlEngine.unmarshal(privilegeFile, YamlAccessModel.class);
        accessModel = new AccessModel(yamlAccessModel);
    }

    @Test
    public void deserializeAndToByteTest() throws IOException, ClassNotFoundException {
        byte[] b = accessModel.toBytes();
        AccessModel accessModel1 = AccessModel.deserialize(b);
        assertThat(accessModel.equals(accessModel1), is(true));
        byte[] userInfoBytes = accessModel.informationToBytes();
        Map<String, UserInformation> userInformation1 = AccessModel.deserializeUserInformation(userInfoBytes);
        byte[] userPrivilegeBytes = accessModel.usersPrivilegeToBytes();
        Map<String, UserPrivilege> userPrivilegeMap = AccessModel.deserializeUsersPrivilege(userPrivilegeBytes);
        byte[] rolePrivilegeBytes = accessModel.rolePrivilegesToBytes();
        Map<String, RolePrivilege> rolePrivilegeMap = AccessModel.deserializeRolePrivileges(rolePrivilegeBytes);
        byte[] invalidGroupBytes = accessModel.invalidGroupToBytes();
        Collection<String> invalidGroups1 = AccessModel.deserializeInvalidGroup(invalidGroupBytes);
        accessModel1.updateInformation(userInformation1);
        accessModel1.updateRolePrivileges(rolePrivilegeMap);
        accessModel1.updateUsersPrivilege(userPrivilegeMap);
        accessModel1.updateInvalidGroup(invalidGroups1);
        assertThat(accessModel.equals(accessModel1), is(true));
        assertThat(accessModel.getRolesPrivileges().size(), is(2));
        assertThat(accessModel.getUsersPrivilege().size(), is(2));
        assertThat(accessModel.getUserInformationMap().get("user3").getPassword(), is("pw3"));
        assertThat(accessModel.getUsersPrivilege().containsKey("user3"), is(false));
    }

    @Test
    public void actionTestManageUser() {
        accessModel.createUser("root", "user3", "x");
        accessModel.createRole("root", "role3");
        assertThat(accessModel.getUserInformationMap().containsKey("user3"), is(true));
        assertThat(accessModel.getRolesPrivileges().containsKey("role3"), is(true));
        accessModel.removeUser("root", "user3");
        accessModel.removeRole("root", "role3");
        assertThat(accessModel.getUserInformationMap().containsKey("user3"), is(false));
        assertThat(accessModel.getRolesPrivileges().containsKey("role3"), is(false));
        accessModel.disableUser("root", "user1");
        assertThat(accessModel.getInvalidUserGroup().contains("user1"), is(true));
    }

    @Test
    public void actionTestCheck() {
        List<String> cols = new LinkedList<>();
        cols.add("col");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "insert",
                "db",
                "table"), is(true));
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "insert",
                "db",
                "table",
                cols), is(true));
    }

    @Test
    public void actionGrantTest() {
        accessModel.createRole("root", "role3");
        // grant user role
        accessModel.grantUser("root", "user1", "role3");
        assertThat(accessModel.getUsersPrivilege().get("user1").getRoles().contains("role3"), is(true));
        // grant user cols
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        cols.add("col2");
        accessModel.grantUser("root",
                "user1",
                "delete",
                "db1",
                "table1",
                cols);
        cols = new LinkedList<>();
        cols.add("col1");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1",
                cols), is(true));
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1"), is(false));
        // grant user table
        accessModel.grantUser("root",
                "user1",
                "delete",
                "db1",
                "table1");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1"), is(true));
        // grant role cols
        cols = new LinkedList<>();
        cols.add("col1");
        cols.add("col2");
        accessModel.grantRole("root",
                "role1",
                "select",
                "db1",
                "table1",
                cols);
        cols = new LinkedList<>();
        cols.add("col1");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "select",
                "db1",
                "table1",
                cols), is(true));
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "select",
                "db1",
                "table1"), is(false));
        // grant role table
        accessModel.grantRole("root",
                "role1",
                "select",
                "db1",
                "table1");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "select",
                "db1",
                "table1"), is(true));
    }

    @Test
    public void actionRevokeTest() {
        accessModel.createRole("root", "role3");
        // revoke user role
        accessModel.revokeUser("root",
                "user1",
                "role1");
        assertThat(accessModel.getUsersPrivilege().get("user1").getRoles().contains("role1"), is(false));
        // revoke user cols
        accessModel.revokeUser("root", "user1", "role2");
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        accessModel.revokeUser("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols), is(true));
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable"), is(false));
        // revoke user table
        accessModel.revokeUser("root",
                "user1",
                "update",
                "testDB",
                "testTable");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable"), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable"), is(false));
        // revoke role cols
        accessModel.grantUser("root",
                "user1",
                "role1");
        cols = new LinkedList<>();
        cols.add("col1");
        accessModel.revokeRole("root",
                "role1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols), is(true));
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable"), is(false));
        // revoke role table
        accessModel.revokeRole("root",
                "role1",
                "update",
                "testDB",
                "testTable");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable"), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        assertThat(accessModel.checkUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable"), is(false));
    }
}
