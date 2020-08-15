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

import lombok.Getter;
import lombok.Setter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.shardingsphere.proxy.backend.privilege.model.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserPrivilege;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class PrivilegeWatcher {

    private final AccessModel accessModelRef;

    private CuratorFramework client;

    @Getter
    @Setter
    private Properties props = new Properties();

    public static String rootPath = "/shardingSpherePrivilegeRoot"
            , subInfoPath = "/userInformation"
            , subRolePrivilegePath = "/rolePrivileges"
            , subUserPrivilegePath = "/userPrivileges"
            , subInvalidUserPath = "/invalidGroup";

    private Stat userInfoStat
            , invalidGroupStat
            , rolePrivilegesStat
            , userPrivilegesStat;

    private NodeCache invalidGroupNode
            , userInfoNode
            , userPrivilegesParentNode
            , rolePrivilegesParentNode;

    @Getter
    private static final int maxUpdateTime = 15;

    public PrivilegeWatcher(final AccessModel accessModel,
                            final String connectString,
                            final int baseSleepTimeMs,
                            final int maxRetries,
                            final String namespace) throws Exception {
        this.accessModelRef = accessModel;
        client = buildClient(connectString, baseSleepTimeMs, maxRetries, namespace);
        client.start();
        addNodeCaches();
        bindListenerToAccessModel();
    }

    /**
     * upload current model to remote zookeeper. And return true if upload successful.
     *
     * @return upload successful
     * @throws Exception upload failed
     */
    public Boolean uploadUserPrivileges() {
        Stat curStat = userPrivilegesStat;
        String userPrivilegePath = rootPath + subUserPrivilegePath;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(userPrivilegePath,accessModelRef.usersPrivilegeToBytes());
            userPrivilegesStat = userPrivilegesParentNode.getCurrentData().getStat();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * upload current model to remote zookeeper. And return true if upload successful.
     *
     * @return upload successful
     * @throws Exception upload failed
     */
    public Boolean uploadRolePrivileges() {
        Stat curStat = rolePrivilegesStat;
        String rolePrivilegePath = rootPath + subRolePrivilegePath;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(rolePrivilegePath,accessModelRef.rolePrivilegesToBytes());
            rolePrivilegesStat = rolePrivilegesParentNode.getCurrentData().getStat();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * upload current model to remote zookeeper. And return true if upload successful.
     *
     * @return upload successful
     * @throws Exception upload failed
     */
    public Boolean uploadUserInformation() {
        Stat curStat = userInfoStat;
        String infoPath = rootPath + subInfoPath;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(infoPath,accessModelRef.informationToBytes());
            userInfoStat = userInfoNode.getCurrentData().getStat();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * upload current model to remote zookeeper. And return true if upload successful.
     *
     * @return upload successful
     * @throws Exception upload failed
     */
    public Boolean uploadInvalidGroup() {
        Stat curStat = invalidGroupStat;
        String invalidPath = rootPath + subInvalidUserPath;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(invalidPath,accessModelRef.invalidGroupToBytes());
            invalidGroupStat = invalidGroupNode.getCurrentData().getStat();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * close all NodeCache and clients.
     *
     * @throws IOException close failed.
     */
    public void closeAll() throws IOException {
        invalidGroupNode.close();
        userInfoNode.close();
        userPrivilegesParentNode.close();
        rolePrivilegesParentNode.close();
        client.close();
    }

    private void flushUserPrivileges(final Map<String, UserPrivilege> remoteUserPrivilege, final Stat remoteStat) {
        if(remoteStat.getVersion() > userPrivilegesStat.getVersion()){
            accessModelRef.updateUsersPrivilege(remoteUserPrivilege);
        }
    }

    private void flushRolePrivileges(final Map<String, RolePrivilege> rolesPrivileges, final Stat remoteStat) {
        if(remoteStat.getVersion() > rolePrivilegesStat.getVersion()){
            accessModelRef.updateRolePrivileges(rolesPrivileges);
        }
    }

    private void flushUserInformation(final Map<String, UserInformation> remoteUserInfo, final Stat remoteStat) {
        if(remoteStat.getVersion() > userInfoStat.getVersion()){
            accessModelRef.updateInformation(remoteUserInfo);
        }
    }

    private void flushInvalidGroup(final Collection<String> invalidUserGroup, final Stat remoteStat) {
        if(remoteStat.getVersion() > invalidGroupStat.getVersion()){
            accessModelRef.updateInvalidGroup(invalidUserGroup);
        }
    }

    private CuratorFramework buildClient(final String connectString,
                                         final int baseSleepTimeMs,
                                         final int maxRetries,
                                         final String namespace){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs,maxRetries);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(retryPolicy)
                .namespace(namespace);
        return builder.build();
    }

    private void addNodeCaches() throws Exception {
        // userInfo
        String tmpCheckPath = rootPath+subInfoPath;
        if (client.checkExists().forPath(tmpCheckPath) == null) {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(tmpCheckPath,accessModelRef.informationToBytes());
        }
        userInfoStat = client.checkExists().forPath(tmpCheckPath);
        userInfoNode = new NodeCache(client, tmpCheckPath);
        // invalidUsers
        tmpCheckPath = rootPath+subInvalidUserPath;
        if (client.checkExists().forPath(tmpCheckPath) == null) {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(tmpCheckPath,accessModelRef.invalidGroupToBytes());
        }
        invalidGroupStat = client.checkExists().forPath(tmpCheckPath);
        invalidGroupNode = new NodeCache(client, tmpCheckPath);
        // user privileges
        tmpCheckPath = rootPath+subUserPrivilegePath;
        if (client.checkExists().forPath(tmpCheckPath) == null) {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(tmpCheckPath,accessModelRef.usersPrivilegeToBytes());
        }
        userPrivilegesStat = client.checkExists().forPath(tmpCheckPath);
        userPrivilegesParentNode = new NodeCache(client, tmpCheckPath);
        // role privileges
        tmpCheckPath = rootPath+subRolePrivilegePath;
        if (client.checkExists().forPath(tmpCheckPath) == null) {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(tmpCheckPath,accessModelRef.rolePrivilegesToBytes());
        }
        rolePrivilegesStat = client.checkExists().forPath(tmpCheckPath);
        rolePrivilegesParentNode = new NodeCache(client, tmpCheckPath);
    }

    private void bindListenerToAccessModel() throws Exception {
        userInfoNode.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                Stat curStat = userInfoNode.getCurrentData().getStat();
                byte[] serializedData = userInfoNode.getCurrentData().getData();
                Map<String, UserInformation> tmpUserInformation =
                        AccessModel.deserializeUserInformation(serializedData);
                flushUserInformation(tmpUserInformation, curStat);
            }
        });
        userInfoNode.start();
        invalidGroupNode.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                Stat curStat = invalidGroupNode.getCurrentData().getStat();
                byte[] serializedData = invalidGroupNode.getCurrentData().getData();
                Collection<String> tmpInvalidGroup =
                        AccessModel.deserializeInvalidGroup(serializedData);
                flushInvalidGroup(tmpInvalidGroup, curStat);
            }
        });
        invalidGroupNode.start();
        userPrivilegesParentNode.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                Stat curStat = userPrivilegesParentNode.getCurrentData().getStat();
                byte[] serializedData = userPrivilegesParentNode.getCurrentData().getData();
                Map<String, UserPrivilege> tmpUsersPrivilege =
                        AccessModel.deserializeUsersPrivilege(serializedData);
                flushUserPrivileges(tmpUsersPrivilege, curStat);
            }
        });
        userPrivilegesParentNode.start();
        rolePrivilegesParentNode.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                Stat curStat = rolePrivilegesParentNode.getCurrentData().getStat();
                byte[] serializedData = rolePrivilegesParentNode.getCurrentData().getData();
                Map<String, RolePrivilege> tmpRolePrivileges =
                        AccessModel.deserializeRolePrivileges(serializedData);
                flushRolePrivileges(tmpRolePrivileges, curStat);
            }
        });
        rolePrivilegesParentNode.start();
    }
}
