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
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
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
        assertThat(accessModel.getRolesPrivileges().size(),is(2));
        assertThat(accessModel.getUsersPrivilege().size(),is(2));
        assertThat(accessModel.getUserInformationMap().get("user3").getPassword(),is("pw3"));
        assertThat(accessModel.getUsersPrivilege().containsKey("user3"),is(false));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("select","testDB.testTable"),is(false));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("delete","testDB.testTable"),is(true));
        assertThat(accessModel.getUsersPrivilege().get("user1").checkPrivilege("select","testDB.testTable.col1"),is(true));
    }
}
