package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;

public class AccessModelTest {
    @Test
    public void constructTest() throws IOException {
        File privilegeFile = new File(
                ShardingConfigurationLoader.class.getResource("/conf/" + "/" + "privilege.yaml")
                        .getFile());
        YamlAccessModel yamlAccessModel = YamlEngine.unmarshal(privilegeFile, YamlAccessModel.class);
        AccessModel accessModel = new AccessModel(yamlAccessModel);
        assertThat(accessModel.getRolesPrivileges().size(),is(2));
        assertThat(accessModel.getUsersPrivilege().size(),is(2));
        assertThat(accessModel.getUserInformationMap().get("user3").getPassword(),is("pw3"));
        assertThat(accessModel.getUsersPrivilege().containsKey("user3"),is(false));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("select","testDB.testTable"),is(false));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("select","testDB.testTable.col1"),is(true));
    }
}
