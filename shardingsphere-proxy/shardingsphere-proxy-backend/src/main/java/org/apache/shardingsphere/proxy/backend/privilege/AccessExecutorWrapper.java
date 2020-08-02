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
     * @param userName create user name
     * @param password create user password
     */
    void createUser(String byUserName, String userName, String password);

    /**
     * create role.
     *
     * @param byUserName by user
     * @param roleName create role name
     */
    void createRole(String byUserName, String roleName);

    /**
     * remove user.
     *
     * @param byUserName by user
     * @param userName remove user name
     */
    void removeUser(String byUserName, String userName);

    /**
     * remove role.
     *
     * @param byUserName by user
     * @param roleName remove role name
     */
    void removeRole(String byUserName, String roleName);

    /**
     * disable user.
     *
     * @param byUserName by user
     * @param userName disable user name
     */
    void disableUser(String byUserName, String userName);

    /**
     * check privilege.
     *
     * @param byUserName by user
     * @param userName check user
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     * @param column target column list
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
     * @param userName check user
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     * @param column target column
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
     * @param userName check user
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     * @return have this privilege
     */
    Boolean checkUserPrivilege(String byUserName,
                                      String userName,
                                      String privilegeType,
                                      String database,
                                      String table);

    /**
     * grant privilege.
     *
     * @param byUserName by user
     * @param userName grant user
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     * @param column target columns
     */
    void grantUser(String byUserName,
                          String userName,
                          String privilegeType,
                          String database,
                          String table,
                          List<String> column);

    /**
     * grant privilege.
     *
     * @param byUserName by user
     * @param userName grant user
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     */
    void grantUser(String byUserName,
                          String userName,
                          String privilegeType,
                          String database,
                          String table);

    /**
     * grant privilege.
     *
     * @param byUserName by user
     * @param userName grant user
     * @param privilegeType privilege type
     * @param information database+table
     */
    void grantUser(String byUserName, String userName, String privilegeType, String information);

    /**
     * grant role.
     *
     * @param byUserName by user
     * @param userName grant user
     * @param roleName grant role
     */
    void grantUser(String byUserName, String userName, String roleName);

    /**
     * grant role.
     *
     * @param byUserName by user
     * @param roleName grant role
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     * @param column target columns
     */
    void grantRole(String byUserName,
                          String roleName,
                          String privilegeType,
                          String database,
                          String table,
                          List<String> column);

    /**
     * grant privilege.
     *
     * @param byUserName by user
     * @param roleName grant role
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     */
    void grantRole(String byUserName,
                          String roleName,
                          String privilegeType,
                          String database,
                          String table);

    /**
     * grant role.
     *
     * @param byUserName by user
     * @param roleName grant role
     * @param privilegeType privilege type
     * @param information database+table
     */
    void grantRole(String byUserName,
                          String roleName,
                          String privilegeType,
                          String information);

    /**
     * revoke user.
     *
     * @param byUserName by user
     * @param userName revoke user
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     * @param column target columns
     */
    void revokeUser(String byUserName,
                           String userName,
                           String privilegeType,
                           String database,
                           String table,
                           List<String> column);

    /**
     * revoke user.
     *
     * @param byUserName by user
     * @param userName revoke user
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     */
    void revokeUser(String byUserName,
                           String userName,
                           String privilegeType,
                           String database,
                           String table);

    /**
     * revoke user.
     *
     * @param byUserName by user
     * @param userName revoke user
     * @param privilegeType privilege type
     * @param information database+table
     */
    void revokeUser(String byUserName,
                           String userName,
                           String privilegeType,
                           String information);

    /**
     * revoke role.
     *
     * @param byUserName by user
     * @param userName revoke user
     * @param roleName role
     */
    void revokeUser(String byUserName, String userName, String roleName);

    /**
     * revoke role.
     *
     * @param byUserName by user
     * @param roleName revoke role
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     * @param column target columns
     */
    void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column);

    /**
     * revoke role.
     *
     * @param byUserName by user
     * @param roleName revoke role
     * @param privilegeType privilege type
     * @param database target database
     * @param table target table
     */
    void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table);

    /**
     * revoke role.
     *
     * @param byUserName by user
     * @param roleName revoke role
     * @param privilegeType privilege type
     * @param information database+table
     */
    void revokeRole(String byUserName, String roleName, String privilegeType, String information);
}
