package org.apache.shardingsphere.proxy.backend.privilege.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.shardingsphere.proxy.backend.privilege.AccessModel;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeAction;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class ZKPrivilegeWatcher {

    private final AccessModel accessModelRef;

    CuratorFramework client;

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

    private int maxUpdateTime = 15;

    public ZKPrivilegeWatcher(AccessModel accessModel,
                              String connectString,
                              int baseSleepTimeMs,
                              int maxRetries) throws Exception {
        this.accessModelRef = accessModel;
        client = buildClient(connectString, baseSleepTimeMs, maxRetries);
        client.start();
        addNodeCaches();
        bindListenerToAccessModel();
        flushLocalModel();
    }

    private CuratorFramework buildClient(String connectString,
                                  int baseSleepTimeMs,
                                  int maxRetries){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs,maxRetries);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(retryPolicy);
        return builder.build();
    }

    private void addNodeCaches() throws Exception {
        // userInfo
        String tmpCheckPath = rootPath+subInfoPath;
        if(client.checkExists().forPath(tmpCheckPath)==null){
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
        if(client.checkExists().forPath(tmpCheckPath)==null){
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
        if(client.checkExists().forPath(tmpCheckPath)==null){
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
        if(client.checkExists().forPath(tmpCheckPath)==null){
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
                if(curStat.getVersion() > userInfoStat.getVersion()){
                    accessModelRef.updateInformation(tmpUserInformation);
                }
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
                if(curStat.getVersion() > invalidGroupStat.getVersion()){
                    accessModelRef.updateInvalidGroup(tmpInvalidGroup);
                }
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
                if(curStat.getVersion() > userPrivilegesStat.getVersion()){
                    accessModelRef.updateUsersPrivilege(tmpUsersPrivilege);
                }
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
                if(curStat.getVersion() > rolePrivilegesStat.getVersion()){
                    accessModelRef.updateRolePrivileges(tmpRolePrivileges);
                }
            }
        });
        rolePrivilegesParentNode.start();
    }

    private void flushLocalModel() throws Exception {
        String infoPath = rootPath + subInfoPath
                , invalidPath = rootPath + subInvalidUserPath
                , userPrivilegePath = rootPath + subUserPrivilegePath
                , rolePrivilegePath = rootPath + subRolePrivilegePath;
        Stat remoteInfoStat = client.checkExists()
                .forPath(infoPath);
        Map<String, UserInformation> tmpUserInfo = AccessModel.deserializeUserInformation(
                client.getData().forPath(infoPath));
        if(!accessModelRef.getUserInformationMap().equals(tmpUserInfo)) {
            accessModelRef.updateInformation(tmpUserInfo);
            userInfoStat = remoteInfoStat;
        }
        Stat remoteInvalidStat = client.checkExists()
                .forPath(invalidPath);
        Collection<String> tmpInvalidGroup = AccessModel.deserializeInvalidGroup(
                client.getData().forPath(invalidPath));
        if(!accessModelRef.getInvalidUserGroup().equals(tmpInvalidGroup)) {
            accessModelRef.updateInvalidGroup(tmpInvalidGroup);
            invalidGroupStat = remoteInvalidStat;
        }
        Stat remoteUserStat = client.checkExists()
                .forPath(userPrivilegePath);
        Map<String, UserPrivilege> tmpUsersPrivilege = AccessModel.deserializeUsersPrivilege(
                client.getData().forPath(userPrivilegePath));
        if(!accessModelRef.getUsersPrivilege().equals(tmpUsersPrivilege)) {
            accessModelRef.updateUsersPrivilege(tmpUsersPrivilege);
            userPrivilegesStat = remoteUserStat;
        }
        Stat remoteRoleStat = client.checkExists()
                .forPath(rolePrivilegePath);
        Map<String, RolePrivilege> tmpRolesPrivilege = AccessModel.deserializeRolePrivileges(
                client.getData().forPath(rolePrivilegePath));
        if(!accessModelRef.getRolesPrivileges().equals(tmpRolesPrivilege)) {
            accessModelRef.updateRolePrivileges(tmpRolesPrivilege);
            rolePrivilegesStat = remoteRoleStat;
        }
    }

    public Boolean updateUserInfo(AccessModel accessModel,
                               PrivilegeAction action)
            throws IOException, ClassNotFoundException {
        Stat curStat = userInfoStat;
        String infoPath = rootPath + subInfoPath;
        int updateTimes = 0;
        boolean updateSuccess;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(infoPath,accessModel.informationToBytes());
            updateSuccess = true;
            curStat = userInfoNode.getCurrentData().getStat();
        }
        catch (Exception e){
            updateSuccess = false;
        }
        while (!updateSuccess) {
            // cur version < remote version, cur model&stat need to update
            Map<String, UserInformation> tmpUserInfo = AccessModel.deserializeUserInformation(
                    userInfoNode.getCurrentData().getData());
            curStat = userInfoNode.getCurrentData().getStat();
            accessModel.updateInformation(tmpUserInfo);
            accessModel.doAction(action);
            try {
                client.setData()
                        .withVersion(curStat.getVersion())
                        .forPath(infoPath,accessModel.informationToBytes());
                updateSuccess = true;
                curStat = userInfoNode.getCurrentData().getStat();
            }
            catch (Exception e){
                updateSuccess = false;
            }
            updateTimes++;
            if (updateTimes > maxUpdateTime && !updateSuccess) return false;
        }
        userInfoStat = curStat;
        return true;
    }

    public Boolean updateInvalidGroup(AccessModel accessModel,
                                  PrivilegeAction action)
            throws IOException, ClassNotFoundException {
        Stat curStat = invalidGroupStat;
        String invalidPath = rootPath + subInvalidUserPath;
        int updateTimes = 0;
        boolean updateSuccess;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(invalidPath,accessModel.invalidGroupToBytes());
            updateSuccess = true;
            curStat = invalidGroupNode.getCurrentData().getStat();
        }
        catch (Exception e){
            updateSuccess = false;
        }
        while (!updateSuccess) {
            // cur version < remote version, cur model&stat need to update
            Collection<String> tmpInvalidGroup = AccessModel.deserializeInvalidGroup(
                    invalidGroupNode.getCurrentData().getData());
            curStat = invalidGroupNode.getCurrentData().getStat();
            accessModel.updateInvalidGroup(tmpInvalidGroup);
            accessModel.doAction(action);
            try {
                client.setData()
                        .withVersion(curStat.getVersion())
                        .forPath(invalidPath,accessModel.invalidGroupToBytes());
                updateSuccess = true;
                curStat = invalidGroupNode.getCurrentData().getStat();
            }
            catch (Exception e){
                updateSuccess = false;
            }
            updateTimes++;
            if (updateTimes > maxUpdateTime && !updateSuccess) return false;
        }
        invalidGroupStat = curStat;
        return true;
    }

    public Boolean updateUsersPrivilege(AccessModel accessModel,
                                      PrivilegeAction action)
            throws IOException, ClassNotFoundException {
        Stat curStat = userPrivilegesStat;
        String userPrivilegePath = rootPath + subUserPrivilegePath;
        int updateTimes = 0;
        boolean updateSuccess;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(userPrivilegePath,accessModel.usersPrivilegeToBytes());
            updateSuccess = true;
            curStat = invalidGroupNode.getCurrentData().getStat();
        }
        catch (Exception e){
            updateSuccess = false;
        }
        while (!updateSuccess) {
            // cur version < remote version, cur model&stat need to update
            Map<String, UserPrivilege> tmpUserPrivilege = AccessModel.deserializeUsersPrivilege(
                    userPrivilegesParentNode.getCurrentData().getData());
            curStat = userPrivilegesParentNode.getCurrentData().getStat();
            accessModel.updateUsersPrivilege(tmpUserPrivilege);
            accessModel.doAction(action);
            try {
                client.setData()
                        .withVersion(curStat.getVersion())
                        .forPath(userPrivilegePath,accessModel.usersPrivilegeToBytes());
                updateSuccess = true;
                curStat = userPrivilegesParentNode.getCurrentData().getStat();
            }
            catch (Exception e){
                updateSuccess = false;
            }
            updateTimes++;
            if (updateTimes > maxUpdateTime && !updateSuccess) return false;
        }
        userPrivilegesStat = curStat;
        return true;
    }

    public Boolean updateRolesPrivilege(AccessModel accessModel,
                                        PrivilegeAction action)
            throws IOException, ClassNotFoundException {
        Stat curStat = rolePrivilegesStat;
        String rolePrivilegePath = rootPath + subRolePrivilegePath;
        int updateTimes = 0;
        boolean updateSuccess;
        try {
            client.setData()
                    .withVersion(curStat.getVersion())
                    .forPath(rolePrivilegePath,accessModel.rolePrivilegesToBytes());
            updateSuccess = true;
            curStat = rolePrivilegesParentNode.getCurrentData().getStat();
        }
        catch (Exception e){
            updateSuccess = false;
        }
        while (!updateSuccess) {
            // cur version < remote version, cur model&stat need to update
            Map<String,RolePrivilege> tmpRolePrivilege = AccessModel.deserializeRolePrivileges(
                    rolePrivilegesParentNode.getCurrentData().getData());
            curStat = rolePrivilegesParentNode.getCurrentData().getStat();
            accessModel.updateRolePrivileges(tmpRolePrivilege);
            accessModel.doAction(action);
            try {
                client.setData()
                        .withVersion(curStat.getVersion())
                        .forPath(rolePrivilegePath,accessModel.rolePrivilegesToBytes());
                updateSuccess = true;
                curStat = rolePrivilegesParentNode.getCurrentData().getStat();
            }
            catch (Exception e){
                updateSuccess = false;
            }
            updateTimes++;
            if (updateTimes > maxUpdateTime && !updateSuccess) return false;
        }
        rolePrivilegesStat = curStat;
        return true;
    }

    public void closeAll() throws IOException {
        invalidGroupNode.close();
        userInfoNode.close();
        userPrivilegesParentNode.close();
        rolePrivilegesParentNode.close();
        client.close();
    }
}