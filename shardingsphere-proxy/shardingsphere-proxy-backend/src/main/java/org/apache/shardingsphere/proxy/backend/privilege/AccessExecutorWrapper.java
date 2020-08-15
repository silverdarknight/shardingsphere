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

import org.apache.shardingsphere.proxy.backend.privilege.model.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserPrivilege;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AccessExecutorWrapper {

    /**
     * create user.
     *
     * @param byUserName by user
     * @param userName target user name
     * @param password target user password
     */
    void createUser(final String byUserName, final String userName, final String password);

    /**
     * create role.
     *
     * @param byUserName by user
     * @param roleName role
     */
    void createRole(final String byUserName, final String roleName);

    /**
     * remove user.
     *
     * @param byUserName by user
     * @param userName user name
     */
    void removeUser(final String byUserName, final String userName);

    /**
     * remove role.
     *
     * @param byUserName by user
     * @param roleName role name
     */
    void removeRole(final String byUserName, final String roleName);

    /**
     * disable user.
     *
     * @param byUserName by user
     * @param userName user name
     */
    void disableUser(final String byUserName, final String userName);

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
    Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table,
                                      final List<String> column);

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
    Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table,
                                      final String column);

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
    Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table);

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
    void grantUser(final String byUserName,
                          final String userName,
                          final String privilegeType,
                          final String database,
                          final String table,
                          final List<String> column);

    /**
     * grant user privilege (table).
     *
     * @param byUserName by user
     * @param userName user
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void grantUser(final String byUserName,
                          final String userName,
                          final String privilegeType,
                          final String database,
                          final String table);

    /**
     * grant user role.
     *
     * @param byUserName by user
     * @param userName target user
     * @param roleName target role
     */
    void grantUser(final String byUserName, final String userName, final String roleName);

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
    void grantRole(final String byUserName,
                          final String roleName,
                          final String privilegeType,
                          final String database,
                          final String table,
                          final List<String> column);

    /**
     * grant role privilege (table).
     *
     * @param byUserName by user
     * @param roleName role
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void grantRole(final String byUserName,
                          final String roleName,
                          final String privilegeType,
                          final String database,
                          final String table);

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
    void revokeUser(final String byUserName,
                           final String userName,
                           final String privilegeType,
                           final String database,
                           final String table,
                           final List<String> column);

    /**
     * revoke user privileges (table).
     *
     * @param byUserName by user
     * @param userName user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void revokeUser(final String byUserName,
                           final String userName,
                           final String privilegeType,
                           final String database,
                           final String table);

    /**
     * revoke user role.
     *
     * @param byUserName by user
     * @param userName user name
     * @param roleName role
     */
    void revokeUser(final String byUserName, final String userName, final String roleName);

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
    void revokeRole(final String byUserName,
                           final String roleName,
                           final String privilegeType,
                           final String database,
                           final String table,
                           final List<String> column);

    /**
     * revoke role privileges (table).
     *
     * @param byUserName by user
     * @param roleName role
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void revokeRole(final String byUserName,
                           final String roleName,
                           final String privilegeType,
                           final String database,
                           final String table);
}
