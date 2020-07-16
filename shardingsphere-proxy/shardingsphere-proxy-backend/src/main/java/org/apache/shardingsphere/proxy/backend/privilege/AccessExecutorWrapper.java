package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;

import java.util.List;

/**
 * byUser-create    -isUser-name+pw
 *                  -name
 *        remove    -isUser-name
 *                  -name
 *        disable   -isUser-name
 *                  -name
 *        revoke    -isUser-name-privilegeType-pathParameters
 *                              -roleName
 *                  -name-privilegeType-pathParameters
 *        grant     -isUser-name-privilegeType-pathParameters
 *                              -roleName
 *                  -name-privilegeType-pathParameters
 *
 *        check     -privilegeType-name-pathParameters
 */
public interface AccessExecutorWrapper {

    public void createUser(String byUserName, String userName, String password);

    public void createRole(String byUserName, String roleName);

    public void removeUser(String byUserName, String userName);

    public void removeRole(String byUserName, String roleName);

    public void disableUser(String byUserName, String userName);

    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, List<String> column);

    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, String column);

    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String information, String table);

    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column);

    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String database, String table, String column);

    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String information, String table);

    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column);

    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table);

    public void grantUser(String byUserName, String userName, String privilegeType, String information);

    public void grantUser(String byUserName, String userName, String roleName);

    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column);

    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table);

    public void grantRole(String byUserName, String roleName, String privilegeType, String information);

    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column);

    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table);

    public void revokeUser(String byUserName, String userName, String privilegeType, String information);

    public void revokeUser(String byUserName, String userName, String roleName);

    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column);

    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table);

    public void revokeRole(String byUserName, String roleName, String privilegeType, String information);
}
