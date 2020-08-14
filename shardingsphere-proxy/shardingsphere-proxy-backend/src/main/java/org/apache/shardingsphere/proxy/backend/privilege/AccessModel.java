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
import org.apache.shardingsphere.proxy.backend.privilege.model.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserPrivilege;
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        return bos.toByteArray();
    }

    @Override
    public byte[] informationToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            infoReadLock.lock();
            oos.writeObject(getUserInformationMap());
        } finally {
            infoReadLock.unlock();
            oos.flush();
            oos.close();
        }
        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }

    @Override
    public byte[] invalidGroupToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            invalidGroupReadLock.lock();
            oos.writeObject(getInvalidUserGroup());
        } finally {
            invalidGroupReadLock.unlock();
            oos.flush();
            oos.close();
        }
        oos.flush();
        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }

    @Override
    public byte[] rolePrivilegesToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            rolePrivilegeReadLock.lock();
            oos.writeObject(getRolesPrivileges());
        } finally {
            rolePrivilegeReadLock.unlock();
            oos.flush();
            oos.close();
        }
        oos.flush();
        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }

    @Override
    public byte[] usersPrivilegeToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            userPrivilegeReadLock.lock();
            oos.writeObject(getUsersPrivilege());
        } finally {
            userPrivilegeReadLock.unlock();
            oos.flush();
            oos.close();
        }
        oos.flush();
        byte[] bytes = bos.toByteArray();
        bos.close();
        return bytes;
    }

    /**
     * create user.
     *
     * @param byUserName by user
     * @param userName target user name
     * @param password target user password
     */
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

    /**
     * create role.
     *
     * @param byUserName by user
     * @param roleName role
     */
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

    /**
     * remove user.
     *
     * @param byUserName by user
     * @param userName user name
     */
    public void removeUser(final String byUserName, final String userName) {
        if (checkHavePermission(byUserName, DCLActionType.REMOVE)) {
            getInvalidUserGroup().remove(userName);
            getUserInformationMap().remove(userName);
            getUsersPrivilege().remove(userName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    /**
     * remove role.
     *
     * @param byUserName by user
     * @param roleName role name
     */
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

    /**
     * disable user.
     *
     * @param byUserName by user
     * @param userName user name
     */
    public void disableUser(final String byUserName, final String userName) {
        if (checkHavePermission(byUserName, DCLActionType.DISABLE)) {
            getInvalidUserGroup().add(userName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    /**
     * check privilege.
     *
     * @param byUserName by user
     * @param userName target user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table name
     * @param column columns name
     * @return have this privilege
     */
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

    /**
     * check privilege.
     *
     * @param byUserName by user
     * @param userName target user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table name
     * @param column column name
     * @return have this privilege
     */
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

    /**
     * check privilege.
     *
     * @param byUserName by user
     * @param userName target user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table name
     * @return have this privilege
     */
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

    /**
     * grant user privileges (column).
     *
     * @param byUserName by user
     * @param userName user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table name
     * @param column columns
     */
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

    /**
     * grant user privilege (table).
     *
     * @param byUserName by user
     * @param userName user
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
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

    /**
     * grant user role.
     *
     * @param byUserName by user
     * @param userName target user
     * @param roleName target role
     */
    public void grantUser(final String byUserName, final String userName, final String roleName) {
        if (checkHavePermission(byUserName, DCLActionType.GRANT)) {
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(roleName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    /**
     * grant role privilege (columns).
     *
     * @param byUserName by user
     * @param roleName role name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table name
     * @param column columns
     */
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

    /**
     * grant role privilege (table).
     *
     * @param byUserName by user
     * @param roleName role
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
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

    /**
     * revoke user privilege (columns).
     *
     * @param byUserName by user
     * @param userName user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table name
     * @param column columns
     */
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

    /**
     * revoke user privileges (table).
     *
     * @param byUserName by user
     * @param userName user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
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

    /**
     * revoke user role.
     *
     * @param byUserName by user
     * @param userName user name
     * @param roleName role
     */
    public void revokeUser(final String byUserName, final String userName, final String roleName) {
        if (checkHavePermission(byUserName, DCLActionType.REVOKE)) {
            getUsersPrivilege().get(userName).revoke(roleName);
        } else {
            throw PrivilegeExceptions.notHaveCurrentPermission();
        }
    }

    /**
     * revoke role privilege (columns).
     *
     * @param byUserName by user
     * @param roleName role name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table name
     * @param column columns
     */
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

    /**
     * revoke role privileges (table).
     *
     * @param byUserName by user
     * @param roleName role
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
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

    private Boolean containsUser(final String userName) {
        return userInformationMap.containsKey(userName);
    }

    private Boolean containsRole(final String roleName) {
        return this.getRolesPrivileges().containsKey(roleName.trim());
    }

    private UserInformation getUser(final String userName) {
        if (!this.getUserInformationMap().containsKey(userName)) {
            throw PrivilegeExceptions.noSuchUser(userName);
        }
        return this.getUserInformationMap().get(userName);
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
}
