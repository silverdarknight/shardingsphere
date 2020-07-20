package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.proxy.backend.privilege.zk.ZKPrivilegeWatcher;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PrivilegeExecutor  implements AccessExecutorWrapper{

    private AccessModel accessModel;

    private ZKPrivilegeWatcher zkPrivilege;

    public PrivilegeExecutor(AccessModel accessModel){
        this.accessModel = accessModel;
    }

    public void setZKWatcher(String connectString,
                             int baseSleepTimeMs,
                             int maxRetries) throws Exception {
        zkPrivilege = new ZKPrivilegeWatcher(accessModel,
                connectString,
                baseSleepTimeMs,
                maxRetries);
    }

    public void closeZKWatcher() throws IOException {
        zkPrivilege.closeAll();
    }

    @Override
    public void createUser(String byUserName, String userName, String password) {
        PrivilegeAction action = PrivilegeAction.addUser(byUserName, userName, password);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUserInfo(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createRole(String byUserName, String roleName) {
        PrivilegeAction action = PrivilegeAction.addRole(byUserName, roleName);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateRolesPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUser(String byUserName, String userName) {
        PrivilegeAction action = PrivilegeAction.removeUser(byUserName, userName);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUserInfo(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeRole(String byUserName, String roleName) {
        PrivilegeAction action = PrivilegeAction.removeRole(byUserName, roleName);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel, action);
            zkPrivilege.updateRolesPrivilege(accessModel, action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disableUser(String byUserName, String userName) {
        PrivilegeAction action = PrivilegeAction.disableUser(byUserName, userName);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateInvalidGroup(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        PrivilegeAction action = PrivilegeAction.checkPrivilege(byUserName
                , userName
                , privilegeType
                , database
                , table
                , column);
        return accessModel.doAction(action);
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, String column) {
        List<String> cols = new LinkedList<>();
        cols.add(column);
        PrivilegeAction action = PrivilegeAction.checkPrivilege(byUserName
                , userName
                , privilegeType
                , database
                , table
                , cols);
        return accessModel.doAction(action);
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table) {
        List<String> cols = new LinkedList<>();
        PrivilegeAction action = PrivilegeAction.checkPrivilege(byUserName
                , userName
                , privilegeType
                , database
                , table
                , cols);
        return accessModel.doAction(action);
    }

    @Override
    public void grantUser(String byUserName,
                          String userName,
                          String privilegeType,
                          String database,
                          String table,
                          List<String> column) {
        PrivilegeAction action = PrivilegeAction.grantUserPrivilege(byUserName
                , userName
                , privilegeType
                , database
                , table
                , column);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table) {
        PrivilegeAction action = PrivilegeAction.grantUserPrivilege(byUserName
                , userName
                , privilegeType
                , database
                , table
                , new LinkedList<String>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String information) {
        String dbName = splitInformation(information)[0],
                tableName = splitInformation(information)[1];
        PrivilegeAction action = PrivilegeAction.grantUserPrivilege(byUserName
                , userName
                , privilegeType
                , dbName
                , tableName
                , new LinkedList<String>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void grantUser(String byUserName, String userName, String roleName) {
        PrivilegeAction action = PrivilegeAction.grantUserRole(byUserName,
                userName,
                roleName);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        PrivilegeAction action = PrivilegeAction.grantRolePrivilege(byUserName,
                roleName,
                privilegeType,
                database, table, column);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateRolesPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        PrivilegeAction action = PrivilegeAction.grantRolePrivilege(byUserName,
                roleName,
                privilegeType,
                database, table, new LinkedList<String>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateRolesPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String information) {
        String dbName = splitInformation(information)[0],
                tableName = splitInformation(information)[1];
        PrivilegeAction action = PrivilegeAction.grantRolePrivilege(byUserName,
                roleName,
                privilegeType,
                dbName, tableName, new LinkedList<String>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateRolesPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        PrivilegeAction action = PrivilegeAction.revokeUserPrivilege(byUserName,
                userName,
                privilegeType,
                database, table, column);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table) {
        PrivilegeAction action = PrivilegeAction.revokeUserPrivilege(byUserName,
                userName,
                privilegeType,
                database, table, new LinkedList<String>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String information) {
        String dbName = splitInformation(information)[0],
                tableName = splitInformation(information)[1];
        PrivilegeAction action = PrivilegeAction.revokeUserPrivilege(byUserName,
                userName,
                privilegeType,
                dbName, tableName, new LinkedList<String>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revokeUser(String byUserName, String userName, String roleName) {
        PrivilegeAction action = PrivilegeAction.revokeUserRole(byUserName,
                userName,
                roleName);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateUsersPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        PrivilegeAction action = PrivilegeAction.revokeRolePrivilege(byUserName,
                roleName,
                privilegeType,
                database,
                table,
                column);
        accessModel.doAction(action);
        try {
            zkPrivilege.updateRolesPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        PrivilegeAction action = PrivilegeAction.revokeRolePrivilege(byUserName,
                roleName,
                privilegeType,
                database,
                table,
                new LinkedList<String>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateRolesPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String information) {
        String dbName = splitInformation(information)[0],
                tableName = splitInformation(information)[1];
        PrivilegeAction action = PrivilegeAction.revokeRolePrivilege(byUserName,
                roleName,
                privilegeType,
                dbName,
                tableName,
                new LinkedList<>());
        accessModel.doAction(action);
        try {
            zkPrivilege.updateRolesPrivilege(accessModel,action);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String[] splitInformation(String information){
        String[] ans = information.split("\\.");
        return ans;
    }
}
