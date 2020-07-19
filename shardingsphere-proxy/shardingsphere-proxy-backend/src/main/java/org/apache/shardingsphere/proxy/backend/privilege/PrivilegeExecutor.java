package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.proxy.backend.privilege.zk.ZKPrivilegeWatcher;

import java.util.List;

public class PrivilegeExecutor  implements PrivilegeExecutorWrapper, AccessExecutorWrapper{

    private AccessModel accessModel;

    private ZKPrivilegeWatcher zkPrivilege;

    private PrivilegeAction waitingAction;

    @Override
    public void setNextAction(PrivilegeAction action) {

    }

    @Override
    public void runAction(PrivilegeAction action) {

    }

    @Override
    public void redoAction(PrivilegeAction action) {

    }

    @Override
    public boolean checkAction(PrivilegeAction action) {
        return false;
    }

    @Override
    public void otherAction(PrivilegeAction action) {

    }

    @Override
    public void createUser(String byUserName, String userName, String password) {

    }

    @Override
    public void createRole(String byUserName, String roleName) {

    }

    @Override
    public void removeUser(String byUserName, String userName) {

    }

    @Override
    public void removeRole(String byUserName, String roleName) {

    }

    @Override
    public void disableUser(String byUserName, String userName) {

    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        return null;
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, String column) {
        return null;
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String information, String table) {
        return null;
    }

    @Override
    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        return null;
    }

    @Override
    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String database, String table, String column) {
        return null;
    }

    @Override
    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String information, String table) {
        return null;
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table) {

    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String information) {

    }

    @Override
    public void grantUser(String byUserName, String userName, String roleName) {

    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table) {

    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String information) {

    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table) {

    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String information) {

    }

    @Override
    public void revokeUser(String byUserName, String userName, String roleName) {

    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table) {

    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String information) {

    }
}
