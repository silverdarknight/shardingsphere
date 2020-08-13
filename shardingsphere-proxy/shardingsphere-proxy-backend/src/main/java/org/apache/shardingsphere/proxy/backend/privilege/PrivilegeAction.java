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
import org.apache.shardingsphere.proxy.backend.privilege.common.DCLActionType;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserInformation;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public final class PrivilegeAction {

    private String byUser = UserInformation.getDefaultUser();

    private DCLActionType actionType;

    private Boolean isUser;

    private String name;

    private String password;

    private String privilegeType;

    private String dbName;

    private String tableName;

    private List<String> columns = new LinkedList<>();

    private String roleName;

    private Boolean privilegePathValid;

    /**
     * construct add user action.
     *
     * @param byUser by user
     * @param name user name
     * @param pw password
     * @return action
     */
    public static PrivilegeAction addUser(final String byUser, final String name, final String pw) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.CREATE);
        action.setIsUser(true);
        action.setName(name);
        action.setPassword(pw);
        return action;
    }

    /**
     * construct add role action.
     *
     * @param byUser by user
     * @param name user name
     * @return action
     */
    public static PrivilegeAction addRole(final String byUser, final String name) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.CREATE);
        action.setIsUser(false);
        action.setName(name);
        return action;
    }

    /**
     * construct remove user action.
     *
     * @param byUser by user
     * @param name user name
     * @return action
     */
    public static PrivilegeAction removeUser(final String byUser, final String name) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.REMOVE);
        action.setIsUser(true);
        action.setName(name);
        return action;
    }

    /**
     * construct remove role action.
     *
     * @param byUser by user
     * @param name user name
     * @return action
     */
    public static PrivilegeAction removeRole(final String byUser, final String name) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.REMOVE);
        action.setIsUser(false);
        action.setName(name);
        return action;
    }

    /**
     * disable user action.
     *
     * @param byUser by user
     * @param name user name
     * @return action
     */
    public static PrivilegeAction disableUser(final String byUser, final String name) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.DISABLE);
        action.setIsUser(true);
        action.setName(name);
        return action;
    }

    /**
     * check privilege action.
     *
     * @param byUser by user
     * @param name user name
     * @param privilegeType check type
     * @param dbName database
     * @param tableName table name
     * @param cols columns
     * @return action
     */
    public static PrivilegeAction checkPrivilege(final String byUser,
                                                 final String name,
                                                 final String privilegeType,
                                                 final String dbName,
                                                 final String tableName,
                                                 final List<String> cols) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.CHECK);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if (cols.size() == 0) {
            action.setColumns(null);
        } else {
            action.setColumns(cols);
        }
        return action;
    }

    /**
     * grant user action.
     *
     * @param byUser by user
     * @param name user name
     * @param privilegeType grant type
     * @param dbName database
     * @param tableName table
     * @param cols columns
     * @return action
     */
    public static PrivilegeAction grantUserPrivilege(final String byUser,
                                                     final String name,
                                                     final String privilegeType,
                                                     final String dbName,
                                                     final String tableName,
                                                     final List<String> cols) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.GRANT);
        action.setIsUser(true);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if (cols.size() == 0) {
            action.setColumns(null);
        } else {
            action.setColumns(cols);
        }
        return action;
    }

    /**
     * grant user role.
     *
     * @param byUser by user
     * @param name user name
     * @param roleName role name
     * @return action
     */
    public static PrivilegeAction grantUserRole(final String byUser,
                                                final String name,
                                                final String roleName) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.GRANT);
        action.setIsUser(true);
        action.setName(name);
        action.setRoleName(roleName);
        return action;
    }

    /**
     * grant role.
     *
     * @param byUser by user
     * @param name user name
     * @param privilegeType privilege type
     * @param dbName database
     * @param tableName table
     * @param cols columns
     * @return action
     */
    public static PrivilegeAction grantRolePrivilege(final String byUser,
                                                     final String name,
                                                     final String privilegeType,
                                                     final String dbName,
                                                     final String tableName,
                                                     final List<String> cols) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.GRANT);
        action.setIsUser(false);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if (cols.size() == 0) {
            action.setColumns(null);
        } else {
            action.setColumns(cols);
        }
        return action;
    }

    /**
     * revoke privilege.
     *
     * @param byUser by user
     * @param name user name
     * @param privilegeType privilege type
     * @param dbName database
     * @param tableName table
     * @param cols columns
     * @return action
     */
    public static PrivilegeAction revokeUserPrivilege(final String byUser,
                                                      final String name,
                                                      final String privilegeType,
                                                      final String dbName,
                                                      final String tableName,
                                                      final List<String> cols) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.REVOKE);
        action.setIsUser(true);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if (cols.size() == 0) {
            action.setColumns(null);
        } else {
            action.setColumns(cols);
        }
        return action;
    }

    /**
     * revoke role.
     *
     * @param byUser by user
     * @param name user name
     * @param roleName role name
     * @return action
     */
    public static PrivilegeAction revokeUserRole(final String byUser,
                                                 final String name,
                                                 final String roleName) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.REVOKE);
        action.setIsUser(true);
        action.setName(name);
        action.setRoleName(roleName);
        return action;
    }

    /**
     * revoke role privilege.
     *
     * @param byUser by user
     * @param name role name
     * @param privilegeType privilege type
     * @param dbName database
     * @param tableName table
     * @param cols columns
     * @return action
     */
    public static PrivilegeAction revokeRolePrivilege(final String byUser,
                                                      final String name,
                                                      final String privilegeType,
                                                      final String dbName,
                                                      final String tableName,
                                                      final List<String> cols) {
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(DCLActionType.REVOKE);
        action.setIsUser(false);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if (cols.size() == 0) {
            action.setColumns(null);
        } else {
            action.setColumns(cols);
        }
        return action;
    }
}
