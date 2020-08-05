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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.common.DCLActionType;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeExceptions;
import org.apache.shardingsphere.proxy.backend.privilege.CommonModel.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.CommonModel.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.CommonModel.UserPrivilege;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlUserPrivilegeConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Getter
public class AccessModel implements AccessExecutorWrapper, Serializable {

    private static final long serialVersionUID = -6691230884488440405L;

    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, UserInformation> userInformationMap = new HashMap<>();

    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, UserPrivilege> usersPrivilege = new HashMap<>();

    @Setter(value = AccessLevel.PRIVATE)
    private Collection<String> invalidUserGroup = new HashSet<>();

    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, RolePrivilege> rolesPrivileges = new HashMap<>();

    private final ReentrantReadWriteLock infoLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock invalidGroupLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock userPrivilegesLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock rolePrivilegesLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock infoReadLock = infoLock.readLock();

    private final ReentrantReadWriteLock.ReadLock invalidGroupReadLock = invalidGroupLock.readLock();

    private final ReentrantReadWriteLock.ReadLock userPrivilegeReadLock = userPrivilegesLock.readLock();

    private final ReentrantReadWriteLock.ReadLock rolePrivilegeReadLock = rolePrivilegesLock.readLock();

    private final ReentrantReadWriteLock.WriteLock infoWriteLock = infoLock.writeLock();

    private final ReentrantReadWriteLock.WriteLock invalidGroupWriteLock = invalidGroupLock.writeLock();

    private final ReentrantReadWriteLock.WriteLock userPrivilegeWriteLock = userPrivilegesLock.writeLock();

    private final ReentrantReadWriteLock.WriteLock rolePrivilegeWriteLock = rolePrivilegesLock.writeLock();

    public AccessModel(final YamlAccessModel yamlAccessModel) {
        // role privileges
        Iterator<Map.Entry<String, YamlPrivilegeConfiguration>> roleIterator = yamlAccessModel
                .getRoleList()
                .entrySet()
                .iterator();
        while (roleIterator.hasNext()) {
            Map.Entry<String, YamlPrivilegeConfiguration> kv = roleIterator.next();
            RolePrivilege tmpRolePrivilege = new RolePrivilege(kv.getKey());
            tmpRolePrivilege.constructModel(kv.getValue());
            this.addRole(tmpRolePrivilege);
        }
        Iterator<Map.Entry<String, YamlUserPrivilegeConfiguration>> userIterator = yamlAccessModel
                .getUserList()
                .entrySet()
                .iterator();
        while (userIterator.hasNext()) {
            Map.Entry<String, YamlUserPrivilegeConfiguration> kv = userIterator.next();
            YamlUserPrivilegeConfiguration curConfig = kv.getValue();
            String userName = kv.getKey();
            // user information
            UserInformation tmpUserInformation = this.addUser(userName, curConfig.getPassword());
            this.getUserInformationMap().put(userName, tmpUserInformation);
            try {
                // user privileges
                UserPrivilege tmpUserPrivilege = new UserPrivilege();
                Iterator<String> roleNamesIterator = curConfig.getRoles().iterator();
                while (roleNamesIterator.hasNext()) {
                    String roleName = roleNamesIterator.next();
                    if (rolesPrivileges.containsKey(roleName)) {
                        tmpUserPrivilege.grant(roleName);
                    }
                }
                tmpUserPrivilege.constructModel(curConfig.getPrivileges());
                this.addUserPrivilege(tmpUserInformation, tmpUserPrivilege);
            } catch (NullPointerException ex) {
                //
            }
        }
        // invalid group
        this.getInvalidUserGroup().addAll(yamlAccessModel.getInvalidGroup());
    }

    /**
     * update user information.
     *
     * @param userInformationMap user information
     */
    public void updateInformation(final Map<String, UserInformation> userInformationMap) {
        try {
            infoWriteLock.lock();
            this.setUserInformationMap(userInformationMap);
        } catch (ShardingSphereException ex) {
            throw PrivilegeExceptions.updateModelFailed();
        } finally {
            infoWriteLock.unlock();
        }
    }

    /**
     * update user privilege.
     *
     * @param userPrivilegeMap user privilege
     */
    public void updateUsersPrivilege(final Map<String, UserPrivilege> userPrivilegeMap) {
        try {
            userPrivilegeWriteLock.lock();
            this.setUsersPrivilege(userPrivilegeMap);
        } catch (ShardingSphereException ex) {
            throw PrivilegeExceptions.updateModelFailed();
        } finally {
            userPrivilegeWriteLock.unlock();
        }
    }

    /**
     * update invalid user.
     *
     * @param invalidUserGroup invalid user group
     */
    public void updateInvalidGroup(final Collection<String> invalidUserGroup) {
        try {
            invalidGroupWriteLock.lock();
            this.setInvalidUserGroup(invalidUserGroup);
        } catch (ShardingSphereException ex) {
            throw PrivilegeExceptions.updateModelFailed();
        } finally {
            invalidGroupWriteLock.unlock();
        }
    }

    /**
     * update role privilege.
     *
     * @param rolePrivilegeMap role privilege
     */
    public void updateRolePrivileges(final Map<String, RolePrivilege> rolePrivilegeMap) {
        try {
            rolePrivilegeWriteLock.lock();
            this.setRolesPrivileges(rolePrivilegeMap);
        } catch (ShardingSphereException ex) {
            throw PrivilegeExceptions.updateModelFailed();
        } finally {
            rolePrivilegeWriteLock.unlock();
        }
    }

    /**
     * access model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        return bos.toByteArray();
    }

    /**
     * information model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    public byte[] informationToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getUserInformationMap());
        oos.flush();
        return bos.toByteArray();
    }

    /**
     * invalidGroup model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    public byte[] invalidGroupToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getInvalidUserGroup());
        oos.flush();
        return bos.toByteArray();
    }

    /**
     * rolePrivileges model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    public byte[] rolePrivilegesToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getRolesPrivileges());
        oos.flush();
        return bos.toByteArray();
    }

    /**
     * usersPrivilege model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    public byte[] usersPrivilegeToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getUsersPrivilege());
        oos.flush();
        return bos.toByteArray();
    }

    /**
     * deserialize access model from bytes.
     *
     * @param serializeData byte data
     * @return model
     * @throws IOException to byte error
     * @throws ClassNotFoundException error model deserialize
     */
    public static AccessModel deserialize(final byte[] serializeData)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        AccessModel accessModel = (AccessModel) ois.readObject();
        return accessModel;
    }

    /**
     * deserialize Information model from bytes.
     *
     * @param serializeData byte data
     * @return model
     * @throws IOException to byte error
     * @throws ClassNotFoundException error model deserialize
     */
    public static Map<String, UserInformation> deserializeUserInformation(final byte[] serializeData)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Map<String, UserInformation> model = (Map<String, UserInformation>) ois.readObject();
        return model;
    }

    /**
     * deserialize InvalidGroup model from bytes.
     *
     * @param serializeData byte data
     * @return model
     * @throws IOException to byte error
     * @throws ClassNotFoundException error model deserialize
     */
    public static Collection<String> deserializeInvalidGroup(final byte[] serializeData)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Collection<String> model = (Collection<String>) ois.readObject();
        return model;
    }

    /**
     * deserialize RolePrivileges model from bytes.
     *
     * @param serializeData byte data
     * @return model
     * @throws IOException to byte error
     * @throws ClassNotFoundException error model deserialize
     */
    public static Map<String, RolePrivilege> deserializeRolePrivileges(final byte[] serializeData)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Map<String, RolePrivilege> model = (Map<String, RolePrivilege>) ois.readObject();
        return model;
    }

    /**
     * deserialize UsersPrivilege model from bytes.
     *
     * @param serializeData byte data
     * @return model
     * @throws IOException to byte error
     * @throws ClassNotFoundException error model deserialize
     */
    public static Map<String, UserPrivilege> deserializeUsersPrivilege(final byte[] serializeData)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Map<String, UserPrivilege> model = (Map<String, UserPrivilege>) ois.readObject();
        return model;
    }

    /**
     * whether contains user.
     *
     * @param userName user name
     * @return contains user
     */
    private Boolean containsUser(final String userName) {
        return userInformationMap.containsKey(userName);
    }

    /**
     * whether contains role.
     *
     * @param roleName role name
     * @return contains role
     */
    private Boolean containsRole(final String roleName) {
        return this.getRolesPrivileges().containsKey(roleName.trim());
    }

    /**
     * get user information model.
     *
     * @param userName user name
     * @return user information model
     */
    private UserInformation getUser(final String userName) {
        if (!this.getUserInformationMap().containsKey(userName)) {
            throw PrivilegeExceptions.noSuchUser(userName);
        }
        return this.getUserInformationMap().get(userName);
    }

    private UserPrivilege getUserPrivilege(final String userName) {
        UserInformation userInformation = this.getUser(userName);
        UserPrivilege userPrivilege = this.getUsersPrivilege().get(userInformation);
        if (userPrivilege == null) {
            throw PrivilegeExceptions.noSuchGrantDefined();
        } else {
            return userPrivilege;
        }
    }

    private RolePrivilege getRolePrivilege(final String roleName) {
        if (this.containsRole(roleName.trim())) {
            return this.getRolesPrivileges().get(roleName.trim());
        }
        throw PrivilegeExceptions.noSuchRole(roleName);
    }

    private UserInformation addUser(final String userName, final String password) {
        if (this.containsUser(userName)) {
            throw PrivilegeExceptions.alreadyHaveUser(userName);
        }
        UserInformation userInformation = new UserInformation(userName, password);
        this.getUserInformationMap().put(userName, userInformation);
        return userInformation;
    }

    private UserPrivilege addUserPrivilege(final UserInformation userInformation, final UserPrivilege userPrivilege) {
        this.getUsersPrivilege().put(userInformation.getUserName(), userPrivilege);
        return userPrivilege;
    }

    private RolePrivilege addRole(final RolePrivilege rolePrivilege) {
        if (!this.getRolesPrivileges().containsKey(rolePrivilege.getRoleName())) {
            this.getRolesPrivileges().put(rolePrivilege.getRoleName(), rolePrivilege);
            return rolePrivilege;
        }
        throw PrivilegeExceptions.alreadyHaveRole(rolePrivilege.getRoleName());
    }

    @Override
    public void createUser(final String byUserName, final String userName, final String password) {
        if (checkHavePermission(byUserName, DCLActionType.CREATE)) {
            UserInformation information = new UserInformation(userName, password);
            if (!this.getUserInformationMap().containsKey(userName)) {
                this.getUserInformationMap().put(userName, information);
            }
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void createRole(final String byUserName, final String roleName) {
        if (checkHavePermission(byUserName, DCLActionType.CREATE)) {
            RolePrivilege information = new RolePrivilege(roleName);
            if (!this.getRolesPrivileges().containsKey(roleName)) {
                this.getRolesPrivileges().put(roleName, information);
            }
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void removeUser(final String byUserName, final String userName) {
        if (checkHavePermission(byUserName, DCLActionType.REMOVE)) {
            getInvalidUserGroup().remove(userName);
            getUserInformationMap().remove(userName);
            getUsersPrivilege().remove(userName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void removeRole(final String byUserName, final String roleName) {
        if (checkHavePermission(byUserName, DCLActionType.REMOVE)) {
            RolePrivilege targetRole = getRolePrivilege(roleName);
            // users revoke role
            Iterator<Map.Entry<String, UserPrivilege>> userPrivilegeIterator = getUsersPrivilege().entrySet().iterator();
            while (userPrivilegeIterator.hasNext()) {
                Map.Entry<String, UserPrivilege> kv = userPrivilegeIterator.next();
                try {
                    kv.getValue().revoke(targetRole.getRoleName());
                } catch (ShardingSphereException ex) {
                    //
                }
            }
            // remove role
            getRolesPrivileges().remove(roleName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void disableUser(final String byUserName, final String userName) {
        if (checkHavePermission(byUserName, DCLActionType.DISABLE)) {
            getInvalidUserGroup().add(userName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table,
                                      final List<String> column) {
        if (checkHavePermission(byUserName, DCLActionType.CHECK)) {
            if (!getUsersPrivilege().containsKey(userName)) {
                return false;
            }
            Iterator<String> iterator = column.iterator();
            while (iterator.hasNext()) {
                if (!checkUserPrivilege(byUserName, userName, privilegeType, database, table, iterator.next())) {
                    return false;
                }
            }
            return true;
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table,
                                      final String column) {
        if (checkHavePermission(byUserName, DCLActionType.CHECK)) {
            if (!getUsersPrivilege().containsKey(userName)) {
                return false;
            }
            boolean selfCheck = getUsersPrivilege().get(userName).checkPrivilege(privilegeType, database, table, column);
            if (selfCheck) {
                return true;
            } else {
                List<String> selfRoles = getUsersPrivilege().get(userName).getRolesName();
                Iterator<String> iterator = selfRoles.iterator();
                while (iterator.hasNext()) {
                    String curRole = iterator.next();
                    RolePrivilege curRoleModel = getRolesPrivileges().get(curRole);
                    return curRoleModel.checkPrivilege(privilegeType, database, table, column);
                }
                return false;
            }
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table) {
        if (checkHavePermission(byUserName, DCLActionType.CHECK)) {
            if (!getUsersPrivilege().containsKey(userName)) {
                return false;
            }
            boolean selfCheck = getUsersPrivilege().get(userName).checkPrivilege(privilegeType, database, table);
            if (selfCheck) {
                return true;
            } else {
                List<String> selfRoles = getUsersPrivilege().get(userName).getRolesName();
                Iterator<String> iterator = selfRoles.iterator();
                while (iterator.hasNext()) {
                    String curRole = iterator.next();
                    RolePrivilege curRoleModel = getRolesPrivileges().get(curRole);
                    return curRoleModel.checkPrivilege(privilegeType, database, table);
                }
                return false;
            }
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void grantUser(final String byUserName,
                          final String userName,
                          final String privilegeType,
                          final String database,
                          final String table,
                          final List<String> column) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(privilegeType, database, table, column);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void grantUser(final String byUserName,
                          final String userName,
                          final String privilegeType,
                          final String database,
                          final String table) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(privilegeType, database, table);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void grantUser(final String byUserName,
                          final String userName,
                          final String privilegeType,
                          final String information) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(privilegeType, information);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void grantUser(final String byUserName, final String userName, final String roleName) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(roleName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void grantRole(final String byUserName,
                          final String roleName,
                          final String privilegeType,
                          final String database,
                          final String table,
                          final List<String> column) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            getRolesPrivileges().get(roleName).grant(privilegeType, database, table, column);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void grantRole(final String byUserName,
                          final String roleName,
                          final String privilegeType,
                          final String database,
                          final String table) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            getRolesPrivileges().get(roleName).grant(privilegeType, database, table);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void grantRole(final String byUserName,
                          final String roleName,
                          final String privilegeType,
                          final String information) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            getRolesPrivileges().get(roleName).grant(privilegeType, information);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void revokeUser(final String byUserName,
                           final String userName,
                           final String privilegeType,
                           final String database,
                           final String table,
                           final List<String> column) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            getUsersPrivilege().get(userName).revoke(privilegeType, database, table, column);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void revokeUser(final String byUserName,
                           final String userName,
                           final String privilegeType,
                           final String database,
                           final String table) {
        if (checkHavePermission(byUserName, DCLActionType.REVOKE)) {
            getUsersPrivilege().get(userName).revoke(privilegeType, database, table);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void revokeUser(final String byUserName,
                           final String userName,
                           final String privilegeType,
                           final String information) {
        if (checkHavePermission(byUserName, DCLActionType.REVOKE)) {
            getUsersPrivilege().get(userName).revoke(privilegeType, information);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void revokeUser(final String byUserName, final String userName, final String roleName) {
        if (checkHavePermission(byUserName, DCLActionType.REVOKE)) {
            getUsersPrivilege().get(userName).revoke(roleName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void revokeRole(final String byUserName,
                           final String roleName,
                           final String privilegeType,
                           final String database,
                           final String table,
                           final List<String> column) {
        if (checkHavePermission(byUserName, DCLActionType.REVOKE)) {
            getRolePrivilege(roleName).revoke(privilegeType, database, table, column);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void revokeRole(final String byUserName,
                           final String roleName,
                           final String privilegeType,
                           final String database,
                           final String table) {
        if (checkHavePermission(byUserName, DCLActionType.REVOKE)) {
            getRolePrivilege(roleName).revoke(privilegeType, database, table);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    @Override
    public void revokeRole(final String byUserName,
                           final String roleName,
                           final String privilegeType,
                           final String information) {
        if (checkHavePermission(byUserName, DCLActionType.REVOKE)) {
            getRolePrivilege(roleName).revoke(privilegeType, information);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    private Boolean checkHavePermission(final String byUser, final DCLActionType actionType) {
        if (getInvalidUserGroup().contains(byUser)) {
            return false;
        }
        return true;
    }

    private void createUserPrivilegeIfNotExist(final String userName) {
        if (!getUsersPrivilege().containsKey(userName)) {
            if (!getUserInformationMap().containsKey(userName)) {
                throw PrivilegeExceptions.noSuchUser(userName);
            } else {
                getUsersPrivilege().put(userName, new UserPrivilege());
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessModel that = (AccessModel) o;
        return Objects.equals(userInformationMap, that.userInformationMap)
                && Objects.equals(usersPrivilege, that.usersPrivilege)
                && Objects.equals(invalidUserGroup, that.invalidUserGroup)
                && Objects.equals(rolesPrivileges, that.rolesPrivileges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userInformationMap, usersPrivilege, invalidUserGroup, rolesPrivileges);
    }

    /**
     * do action for model.
     *
     * @param action action
     * @return if action is check return check return
     */
    public Boolean doAction(final PrivilegeAction action) {
        String byUser = action.getByUser();
        if (DCLActionType.CREATE == action.getActionType()) {
            if (action.getIsUser()) {
                createUserAction(action);
            } else {
                createRoleAction(action);
            }
        } else if (DCLActionType.REMOVE == action.getActionType()) {
            if (action.getIsUser()) {
                removeUserAction(action);
            } else {
                removeRoleAction(action);
            }
        } else if (DCLActionType.DISABLE == action.getActionType()) {
            disableUserAction(action);
        } else if (DCLActionType.CHECK == action.getActionType()) {
            List<String> cols = action.getColumns();
            return cols == null ? checkUserPrivilegeTable(action)
                    : checkUserPrivilegeColumns(action);
        } else if (action.getActionType() == DCLActionType.GRANT) {
            if (action.getRoleName() != null) {
                grantUserRoleAction(action);
            } else if (action.getIsUser()) {
                grantUserAction(action);
            } else if (!action.getIsUser()) {
                grantRoleAction(action);
            }
        } else if (action.getActionType() == DCLActionType.REVOKE) {
            String name = action.getName();
            if (action.getRoleName() != null) {
                revokeUserRoleAction(action);
            } else if (action.getIsUser()) {
                revokeUserAction(action);
            } else if (!action.getIsUser()) {
                revokeRoleAction(action);
            }
        } else {
            throw PrivilegeExceptions.actionTypeErrorException();
        }
        return true;
    }

    private void createUserAction(final PrivilegeAction action) {
        String password = action.getPassword();
        try {
            infoWriteLock.lock();
            createUser(action.getByUser(), action.getName(), password);
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            infoWriteLock.unlock();
        }
    }

    private void createRoleAction(final PrivilegeAction action) {
        try {
            infoWriteLock.lock();
            createRole(action.getByUser(), action.getName());
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            infoWriteLock.unlock();
        }
    }

    private void removeUserAction(final PrivilegeAction action) {
        try {
            infoWriteLock.lock();
            removeUser(action.getByUser(), action.getName());
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            infoWriteLock.unlock();
        }
    }

    private void removeRoleAction(final PrivilegeAction action) {
        try {
            rolePrivilegeWriteLock.lock();
            removeRole(action.getByUser(), action.getName());
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            rolePrivilegeWriteLock.unlock();
        }
    }

    private void disableUserAction(final PrivilegeAction action) {
        try {
            invalidGroupWriteLock.lock();
            disableUser(action.getByUser(), action.getName());
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            invalidGroupWriteLock.unlock();
        }
    }

    private Boolean checkUserPrivilegeTable(final PrivilegeAction action) {
        String name = action.getName();
        String privilegeType = action.getPrivilegeType();
        String dbName = action.getDbName();
        String tableName = action.getTableName();
        try {
            userPrivilegeReadLock.lock();
            return checkUserPrivilege(action.getByUser(), name, privilegeType, dbName, tableName);
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            userPrivilegeReadLock.unlock();
        }
    }

    private Boolean checkUserPrivilegeColumns(final PrivilegeAction action) {
        String name = action.getName();
        String privilegeType = action.getPrivilegeType();
        String dbName = action.getDbName();
        String tableName = action.getTableName();
        List<String> cols = action.getColumns();
        try {
            userPrivilegeReadLock.lock();
            return checkUserPrivilege(action.getByUser(), name, privilegeType, dbName, tableName, cols);
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            userPrivilegeReadLock.unlock();
        }
    }

    private void grantUserRoleAction(final PrivilegeAction action) {
        String name = action.getName();
        try {
            userPrivilegeWriteLock.lock();
            grantUser(action.getByUser(), name, action.getRoleName());
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            userPrivilegeWriteLock.unlock();
        }
    }

    private void grantUserAction(final PrivilegeAction action) {
        String name = action.getName();
        String privilegeType = action.getPrivilegeType();
        String dbName = action.getDbName();
        String tableName = action.getTableName();
        List<String> cols = action.getColumns();
        if (cols == null) {
            try {
                userPrivilegeWriteLock.lock();
                grantUser(action.getByUser(), name, privilegeType, dbName, tableName);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                userPrivilegeWriteLock.unlock();
            }
        } else {
            try {
                userPrivilegeWriteLock.lock();
                grantUser(action.getByUser(), name, privilegeType, dbName, tableName, cols);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                userPrivilegeWriteLock.unlock();
            }
        }
    }

    private void grantRoleAction(final PrivilegeAction action) {
        String name = action.getName();
        String privilegeType = action.getPrivilegeType();
        String dbName = action.getDbName();
        String tableName = action.getTableName();
        List<String> cols = action.getColumns();
        if (cols == null) {
            try {
                rolePrivilegeWriteLock.lock();
                grantRole(action.getByUser(), name, privilegeType, dbName, tableName);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                rolePrivilegeWriteLock.unlock();
            }
        } else {
            try {
                rolePrivilegeWriteLock.lock();
                grantRole(action.getByUser(), name, privilegeType, dbName, tableName, cols);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                rolePrivilegeWriteLock.unlock();
            }
        }
    }

    private void revokeUserRoleAction(final PrivilegeAction action) {
        String name = action.getName();
        try {
            userPrivilegeWriteLock.lock();
            revokeUser(action.getByUser(), name, action.getRoleName());
        } catch (ShardingSphereException e) {
            throw new ShardingSphereException(e.getMessage());
        } finally {
            userPrivilegeWriteLock.unlock();
        }
    }

    private void revokeUserAction(final PrivilegeAction action) {
        String privilegeType = action.getPrivilegeType();
        String name = action.getName();
        String dbName = action.getDbName();
        String tableName = action.getTableName();
        List<String> cols = action.getColumns();
        if (cols == null) {
            try {
                userPrivilegeWriteLock.lock();
                revokeUser(action.getByUser(), name, privilegeType, dbName, tableName);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                userPrivilegeWriteLock.unlock();
            }
        } else {
            try {
                userPrivilegeWriteLock.lock();
                revokeUser(action.getByUser(), name, privilegeType, dbName, tableName, cols);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                userPrivilegeWriteLock.unlock();
            }
        }
    }

    private void revokeRoleAction(final PrivilegeAction action) {
        String privilegeType = action.getPrivilegeType();
        String name = action.getName();
        String dbName = action.getDbName();
        String tableName = action.getTableName();
        List<String> cols = action.getColumns();
        if (cols == null) {
            try {
                rolePrivilegeWriteLock.lock();
                revokeRole(action.getByUser(), name, privilegeType, dbName, tableName);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                rolePrivilegeWriteLock.unlock();
            }
        } else {
            try {
                rolePrivilegeWriteLock.lock();
                revokeRole(action.getByUser(), name, privilegeType, dbName, tableName, cols);
            } catch (ShardingSphereException e) {
                throw new ShardingSphereException(e.getMessage());
            } finally {
                rolePrivilegeWriteLock.unlock();
            }
        }
    }
}
