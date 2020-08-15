package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeExceptions;

import java.util.List;

public class PrivilegeManager implements AccessExecutorWrapper{

    private AccessModel accessModel;

    private PrivilegeWatcher privilegeWatcher;

    public PrivilegeManager(AccessModel inputModel,
                            String connectString,
                            int baseSleepTimeMs,
                            int maxRetries,
                            String namespace) throws Exception {
        accessModel = inputModel;
        privilegeWatcher = new PrivilegeWatcher(accessModel,
                connectString,
                baseSleepTimeMs,
                maxRetries,
                namespace);
    }

    @Override
    public void createUser(String byUserName, String userName, String password) {
        accessModel.createUser(byUserName, userName, password);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.createUser(byUserName, userName, password);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void createRole(String byUserName, String roleName) {
        accessModel.createRole(byUserName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.createRole(byUserName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void removeUser(String byUserName, String userName) {
        accessModel.removeUser(byUserName, userName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.removeUser(byUserName, userName);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void removeRole(String byUserName, String roleName) {
        accessModel.removeRole(byUserName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserInformation();
            if (!uploadSuccess) {
                // redo action
                accessModel.removeRole(byUserName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void disableUser(String byUserName, String userName) {
        accessModel.disableUser(byUserName, userName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadInvalidGroup();
            if (!uploadSuccess) {
                // redo action
                accessModel.disableUser(byUserName, userName);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        return accessModel.checkUserPrivilege(byUserName, userName, privilegeType, database, table, column);
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, String column) {
        return accessModel.checkUserPrivilege(byUserName, userName, privilegeType, database, table, column);
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table) {
        return accessModel.checkUserPrivilege(byUserName, userName, privilegeType, database, table);
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        accessModel.grantUser(byUserName, userName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantUser(byUserName, userName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table) {
        accessModel.grantUser(byUserName, userName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantUser(byUserName, userName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void grantUser(String byUserName, String userName, String roleName) {
        accessModel.grantUser(byUserName, userName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantUser(byUserName, userName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        accessModel.grantRole(byUserName, roleName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantRole(byUserName, roleName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        accessModel.grantRole(byUserName, roleName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.grantRole(byUserName, roleName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        accessModel.revokeUser(byUserName, userName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeUser(byUserName, userName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table) {
        accessModel.revokeUser(byUserName, userName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeUser(byUserName, userName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void revokeUser(String byUserName, String userName, String roleName) {
        accessModel.revokeUser(byUserName, userName, roleName);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadUserPrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeUser(byUserName, userName, roleName);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        accessModel.revokeRole(byUserName, roleName, privilegeType, database, table, column);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeRole(byUserName, roleName, privilegeType, database, table, column);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        accessModel.revokeRole(byUserName, roleName, privilegeType, database, table);
        int retryNum = 0;
        Boolean uploadSuccess = false;
        while (!uploadSuccess && retryNum < PrivilegeWatcher.getMaxUpdateTime()) {
            uploadSuccess = privilegeWatcher.uploadRolePrivileges();
            if (!uploadSuccess) {
                // redo action
                accessModel.revokeRole(byUserName, roleName, privilegeType, database, table);
                retryNum++;
            }
        }
        if (!uploadSuccess) throw PrivilegeExceptions.UploadZookeeperError();
    }
}
