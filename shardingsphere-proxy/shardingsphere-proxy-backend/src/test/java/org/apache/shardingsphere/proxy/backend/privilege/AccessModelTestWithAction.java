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

public class AccessModelTestWithAction {

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
        PrivilegeAction createUserAction = PrivilegeAction.addUser("root", "user3", "x");
        PrivilegeAction createRoleAction = PrivilegeAction.addRole("root", "role3");
        accessModel.doAction(createUserAction);
        accessModel.doAction(createRoleAction);
        assertThat(accessModel.getUserInformationMap().containsKey("user3"), is(true));
        assertThat(accessModel.getRolesPrivileges().containsKey("role3"), is(true));
        PrivilegeAction removeUserAction = PrivilegeAction.removeUser("root", "user3");
        PrivilegeAction removeRoleAction = PrivilegeAction.removeRole("root", "role3");
        accessModel.doAction(removeUserAction);
        accessModel.doAction(removeRoleAction);
        assertThat(accessModel.getUserInformationMap().containsKey("user3"), is(false));
        assertThat(accessModel.getRolesPrivileges().containsKey("role3"), is(false));
        PrivilegeAction disableUserAction = PrivilegeAction.disableUser("root", "user1");
        accessModel.doAction(disableUserAction);
        assertThat(accessModel.getInvalidUserGroup().contains("user1"), is(true));
    }

    @Test
    public void actionTestCheck() {
        PrivilegeAction checkUserTableAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "insert",
                "db",
                "table",
                new LinkedList<>());
        List<String> cols = new LinkedList<>();
        cols.add("col");
        PrivilegeAction checkUserColAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "insert",
                "db",
                "table",
                cols);
        assertThat(accessModel.doAction(checkUserTableAction), is(true));
        assertThat(accessModel.doAction(checkUserColAction), is(true));
    }

    @Test
    public void actionGrantTest() {
        PrivilegeAction addRoleAction = PrivilegeAction.addRole("root", "role3");
        accessModel.doAction(addRoleAction);
        // grant user role
        PrivilegeAction grantUserRoleAction = PrivilegeAction.grantUserRole("root",
                "user1",
                "role3");
        accessModel.doAction(grantUserRoleAction);
        assertThat(accessModel.getUsersPrivilege().get("user1").getRoles().contains("role3"), is(true));
        // grant user cols
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        cols.add("col2");
        PrivilegeAction grantUserColsAction = PrivilegeAction.grantUserPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1",
                cols);
        accessModel.doAction(grantUserColsAction);
        cols = new LinkedList<>();
        cols.add("col1");
        PrivilegeAction checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1",
                cols);
        assertThat(accessModel.doAction(checkAction), is(true));
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(false));
        // grant user table
        PrivilegeAction grantUserTableAction = PrivilegeAction.grantUserPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1",
                new LinkedList<>());
        accessModel.doAction(grantUserTableAction);
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "delete",
                "db1",
                "table1",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(true));
        // grant role cols
        cols = new LinkedList<>();
        cols.add("col1");
        cols.add("col2");
        PrivilegeAction grantRoleColsAction = PrivilegeAction.grantRolePrivilege("root",
                "role1",
                "select",
                "db1",
                "table1",
                cols);
        accessModel.doAction(grantRoleColsAction);
        cols = new LinkedList<>();
        cols.add("col1");
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "select",
                "db1",
                "table1",
                cols);
        assertThat(accessModel.doAction(checkAction), is(true));
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "select",
                "db1",
                "table1",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(false));
        // grant role table
        PrivilegeAction grantRoleTableAction = PrivilegeAction.grantRolePrivilege("root",
                "role1",
                "select",
                "db1",
                "table1",
                new LinkedList<>());
        accessModel.doAction(grantRoleTableAction);
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "select",
                "db1",
                "table1",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(true));
    }

    @Test
    public void actionRevokeTest() {
        PrivilegeAction addRoleAction = PrivilegeAction.addRole("root", "role3");
        accessModel.doAction(addRoleAction);
        // revoke user role
        PrivilegeAction revokeUserRoleAction = PrivilegeAction.revokeUserRole("root",
                "user1",
                "role1");
        accessModel.doAction(revokeUserRoleAction);
        assertThat(accessModel.getUsersPrivilege().get("user1").getRoles().contains("role1"), is(false));
        // revoke user cols
        revokeUserRoleAction = PrivilegeAction.revokeUserRole("root", "user1", "role2");
        accessModel.doAction(revokeUserRoleAction);
        List<String> cols = new LinkedList<>();
        cols.add("col1");
        PrivilegeAction revokeUserColsAction = PrivilegeAction.revokeUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        accessModel.doAction(revokeUserColsAction);
        PrivilegeAction checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.doAction(checkAction), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.doAction(checkAction), is(true));
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(false));
        // revoke user table
        PrivilegeAction revokeUserTableAction = PrivilegeAction.revokeUserPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                new LinkedList<>());
        accessModel.doAction(revokeUserTableAction);
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.doAction(checkAction), is(false));
        // revoke role cols
        PrivilegeAction grantUserRoleAction = PrivilegeAction.grantUserRole("root",
                "user1",
                "role1");
        accessModel.doAction(grantUserRoleAction);
        cols = new LinkedList<>();
        cols.add("col1");
        PrivilegeAction revokeRoleColsAction = PrivilegeAction.revokeRolePrivilege("root",
                "role1",
                "update",
                "testDB",
                "testTable",
                cols);
        accessModel.doAction(revokeRoleColsAction);
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.doAction(checkAction), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.doAction(checkAction), is(true));
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(false));
        // revoke role table
        PrivilegeAction revokeRoleTableAction = PrivilegeAction.revokeRolePrivilege("root",
                "role1",
                "update",
                "testDB",
                "testTable",
                new LinkedList<>());
        accessModel.doAction(revokeRoleTableAction);
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                new LinkedList<>());
        assertThat(accessModel.doAction(checkAction), is(false));
        cols = new LinkedList<>();
        cols.add("col2");
        checkAction = PrivilegeAction.checkPrivilege("root",
                "user1",
                "update",
                "testDB",
                "testTable",
                cols);
        assertThat(accessModel.doAction(checkAction), is(false));
    }
}
