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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.backend.privilege.zk.ZKPrivilegeWatcher;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ZkWatcherTest {

    @Test
    public void createNodeTest() throws Exception {
        String connectString = "172.18.146.199:2181";
        File privilegeFile = new File(
                ShardingConfigurationLoader.class.getResource("/conf/" + "/" + "privilege.yaml")
                        .getFile());
        YamlAccessModel yamlAccessModel = YamlEngine.unmarshal(privilegeFile, YamlAccessModel.class);
        AccessModel accessModel = new AccessModel(yamlAccessModel);
        PrivilegeExecutor privilegeExecutor = new PrivilegeExecutor(accessModel);
        privilegeExecutor.setZKWatcher(connectString, 5000, 3);
        assertThat(privilegeExecutor.checkUserPrivilege("root","user1","delete","testDB","testTable"),is(true));
        assertThat(privilegeExecutor.checkUserPrivilege("root","user1","delete","testDB","testTable2"),is(false));
        assertThat(privilegeExecutor.checkUserPrivilege("root","user1","delete","testDB","testTable","error"),is(true));
        new Thread(){
            @SneakyThrows
            @Override
            public void run() {
                super.run();
                String connectString = "172.18.146.199:2181";
                File privilegeFile = new File(
                        ShardingConfigurationLoader.class.getResource("/conf/" + "/" + "privilege.yaml")
                                .getFile());
                YamlAccessModel yamlAccessModel = YamlEngine.unmarshal(privilegeFile, YamlAccessModel.class);
                AccessModel accessModel = new AccessModel(yamlAccessModel);
                PrivilegeExecutor privilegeExecutor = new PrivilegeExecutor(accessModel);
                privilegeExecutor.setZKWatcher(connectString, 5000, 3);
                privilegeExecutor.grantUser("root","user3","insert","testDB","testTable");
                privilegeExecutor.closeZKWatcher();
            }
        }.start();
        assertThat(privilegeExecutor.checkUserPrivilege("root","user3","insert","testDB","testTable"),is(false));
        Thread.sleep(5000);
        assertThat(privilegeExecutor.checkUserPrivilege("root","user3","insert","testDB","testTable"),is(true));
        privilegeExecutor.closeZKWatcher();
    }
}
