package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.*;
import java.util.Collection;
import java.util.Map;

public class AccessModelTest {
    @Test
    public void constructTest() throws IOException,ClassNotFoundException {
        File privilegeFile = new File(
                ShardingConfigurationLoader.class.getResource("/conf/" + "/" + "privilege.yaml")
                        .getFile());
        YamlAccessModel yamlAccessModel = YamlEngine.unmarshal(privilegeFile, YamlAccessModel.class);
        AccessModel accessModel = new AccessModel(yamlAccessModel);
        byte[] b = accessModel.toBytes();
        AccessModel accessModel1 = AccessModel.deserialize(b);
        assertThat(accessModel.equals(accessModel1),is(true));
        b = accessModel.informationToBytes();
        Map<String, UserInformation> userInformationMap = AccessModel.deserializeUserInformation(b);
        assertThat(userInformationMap.equals(accessModel.getUserInformationMap()),is(true));
        b = accessModel.rolePrivilegesToBytes();
        Map<String, RolePrivilege> rolePrivilegeMap = AccessModel.deserializeRolePrivileges(b);
        assertThat(rolePrivilegeMap.equals(accessModel.getRolesPrivileges()),is(true));
        b = accessModel.usersPrivilegeToBytes();
        Map<String, UserPrivilege> userPrivilegeMap = AccessModel.deserializeUsersPrivilege(b);
        assertThat(userPrivilegeMap.equals(accessModel.getUsersPrivilege()),is(true));
        b = accessModel.invalidGroupToBytes();
        Collection<String> invalidGroupCollection = AccessModel.deserializeInvalidGroup(b);
        assertThat(invalidGroupCollection.equals(accessModel.getInvalidUserGroup()),is(true));
        assertThat(accessModel.getRolesPrivileges().size(),is(2));
        assertThat(accessModel.getUsersPrivilege().size(),is(2));
        assertThat(accessModel.getUserInformationMap().get("user3").getPassword(),is("pw3"));
        assertThat(accessModel.getUsersPrivilege().containsKey("user3"),is(false));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("select","testDB.testTable"),is(false));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("delete","testDB.testTable"),is(true));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("select","testDB.testTable.col1"),is(true));
    }
}
