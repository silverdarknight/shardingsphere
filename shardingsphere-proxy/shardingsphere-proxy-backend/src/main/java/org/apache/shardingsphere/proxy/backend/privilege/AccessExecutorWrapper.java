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

import java.util.List;

public interface AccessExecutorWrapper {

    /**
     * create user.
     *
     * @param byUserName by user
     * @param userName target user name
     * @param password target user password
     */
    void createUser(String byUserName, String userName, String password);

    /**
     * create role.
     *
     * @param byUserName by user
     * @param roleName role
     */
    void createRole(String byUserName, String roleName);

    /**
     * remove user.
     *
     * @param byUserName by user
     * @param userName user name
     */
    void removeUser(String byUserName, String userName);

    /**
     * remove role.
     *
     * @param byUserName by user
     * @param roleName role name
     */
    void removeRole(String byUserName, String roleName);

    /**
     * disable user.
     *
     * @param byUserName by user
     * @param userName user name
     */
    void disableUser(String byUserName, String userName);

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
    Boolean checkUserPrivilege(String byUserName,
                                      String userName,
                                      String privilegeType,
                                      String database,
                                      String table,
                                      List<String> column);

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
    Boolean checkUserPrivilege(String byUserName,
                                      String userName,
                                      String privilegeType,
                                      String database,
                                      String table,
                                      String column);

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
    Boolean checkUserPrivilege(String byUserName,
                                      String userName,
                                      String privilegeType,
                                      String database,
                                      String table);

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
    void grantUser(String byUserName,
                          String userName,
                          String privilegeType,
                          String database,
                          String table,
                          List<String> column);

    /**
     * grant user privilege (table).
     *
     * @param byUserName by user
     * @param userName user
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void grantUser(String byUserName,
                          String userName,
                          String privilegeType,
                          String database,
                          String table);

    /**
     * grant user role.
     *
     * @param byUserName by user
     * @param userName target user
     * @param roleName target role
     */
    void grantUser(String byUserName, String userName, String roleName);

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
    void grantRole(String byUserName,
                          String roleName,
                          String privilegeType,
                          String database,
                          String table,
                          List<String> column);

    /**
     * grant role privilege (table).
     *
     * @param byUserName by user
     * @param roleName role
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void grantRole(String byUserName,
                          String roleName,
                          String privilegeType,
                          String database,
                          String table);

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
    void revokeUser(String byUserName,
                           String userName,
                           String privilegeType,
                           String database,
                           String table,
                           List<String> column);

    /**
     * revoke user privileges (table).
     *
     * @param byUserName by user
     * @param userName user name
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void revokeUser(String byUserName,
                           String userName,
                           String privilegeType,
                           String database,
                           String table);

    /**
     * revoke user role.
     *
     * @param byUserName by user
     * @param userName user name
     * @param roleName role
     */
    void revokeUser(String byUserName, String userName, String roleName);

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
    void revokeRole(String byUserName,
                           String roleName,
                           String privilegeType,
                           String database,
                           String table,
                           List<String> column);

    /**
     * revoke role privileges (table).
     *
     * @param byUserName by user
     * @param roleName role
     * @param privilegeType privilege type
     * @param database db name
     * @param table table
     */
    void revokeRole(String byUserName,
                           String roleName,
                           String privilegeType,
                           String database,
                           String table);
}
