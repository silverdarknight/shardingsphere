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
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeExceptions;

import java.util.List;

public class PrivilegeManager implements AccessExecutorWrapper {

    private final AccessModel accessModel;

    @Getter
    private final PrivilegeWatcher privilegeWatcher;

    public PrivilegeManager(final AccessModel inputModel,
                            final String connectString,
                            final int baseSleepTimeMs,
                            final int maxRetries,
                            final String namespace) throws Exception {
        accessModel = inputModel;
        privilegeWatcher = new PrivilegeWatcher(accessModel,
                connectString,
                baseSleepTimeMs,
                maxRetries,
                namespace);
    }

    @Override
    public void createUser(final String byUserName, final String userName, final String password) {
        accessModel.createUser(byUserName, userName, password);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.createUser(byUserName, userName, password);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void createRole(final String byUserName, final String roleName) {
        accessModel.createRole(byUserName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.createRole(byUserName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void removeUser(final String byUserName, final String userName) {
        accessModel.removeUser(byUserName, userName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.removeUser(byUserName, userName);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void removeRole(final String byUserName, final String roleName) {
        accessModel.removeRole(byUserName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.removeRole(byUserName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void disableUser(final String byUserName, final String userName) {
        accessModel.disableUser(byUserName, userName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadInvalidGroup();
            if (!uploadSuccess) {
                // redo action
                accessModel.disableUser(byUserName, userName);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table,
                                      final List<String> column) {
        return accessModel.checkUserPrivilege(byUserName, userName, privilegeType, database, table, column);
    }

    @Override
    public Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table,
                                      final String column) {
        return accessModel.checkUserPrivilege(byUserName, userName, privilegeType, database, table, column);
    }

    @Override
    public Boolean checkUserPrivilege(final String byUserName,
                                      final String userName,
                                      final String privilegeType,
                                      final String database,
                                      final String table) {
        return accessModel.checkUserPrivilege(byUserName, userName, privilegeType, database, table);
    }

    @Override
    public void grantUser(final String byUserName,
                          final String userName,
                          final String privilegeType,
                          final String database,
                          final String table,
                          final List<String> column) {
        accessModel.grantUser(byUserName, userName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantUser(byUserName, userName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void grantUser(final String byUserName,
                          final String userName,
                          final String privilegeType,
                          final String database,
                          final String table) {
        accessModel.grantUser(byUserName, userName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantUser(byUserName, userName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void grantUser(final String byUserName,
                          final String userName,
                          final String roleName) {
        accessModel.grantUser(byUserName, userName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantUser(byUserName, userName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void grantRole(final String byUserName,
                          final String roleName,
                          final String privilegeType,
                          final String database,
                          final String table,
                          final List<String> column) {
        accessModel.grantRole(byUserName, roleName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantRole(byUserName, roleName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void grantRole(final String byUserName,
                          final String roleName,
                          final String privilegeType,
                          final String database,
                          final String table) {
        accessModel.grantRole(byUserName, roleName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantRole(byUserName, roleName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void revokeUser(final String byUserName,
                           final String userName,
                           final String privilegeType,
                           final String database,
                           final String table,
                           final List<String> column) {
        accessModel.revokeUser(byUserName, userName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeUser(byUserName, userName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void revokeUser(final String byUserName,
                           final String userName,
                           final String privilegeType,
                           final String database,
                           final String table) {
        accessModel.revokeUser(byUserName, userName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeUser(byUserName, userName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void revokeUser(final String byUserName,
                           final String userName,
                           final String roleName) {
        accessModel.revokeUser(byUserName, userName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeUser(byUserName, userName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void revokeRole(final String byUserName,
                           final String roleName,
                           final String privilegeType,
                           final String database,
                           final String table,
                           final List<String> column) {
        accessModel.revokeRole(byUserName, roleName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeRole(byUserName, roleName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }

    @Override
    public void revokeRole(final String byUserName,
                           final String roleName,
                           final String privilegeType,
                           final String database,
                           final String table) {
        accessModel.revokeRole(byUserName, roleName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMAX_UPDATE_TIME()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeRole(byUserName, roleName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) {
            throw PrivilegeExceptions.uploadZookeeperError();
        }
    }
}
