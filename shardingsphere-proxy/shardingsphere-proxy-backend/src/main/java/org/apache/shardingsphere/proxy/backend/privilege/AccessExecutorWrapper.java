package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;

import java.util.List;

public interface AccessExecutorWrapper {

    public Boolean checkUserPrivilege(String userName, String privilegeType, String database, String table, List<String> column);

    public Boolean checkUserPrivilege(String userName, String privilegeType, String database, String table, String column);

    public Boolean checkUserPrivilege(String userName, String privilegeType, String database, String table);

    public Boolean checkUserPrivilege(String userName, String privilegeType, String database);

    public void createUser(String userName, String password);

    public void removeUser(String userName);

    public void grantUser(String userName, String privilegeType, String database, String table, List<String> column);

    public void grantUser(String userName, String privilegeType, String database, String table);

    public void grantUser(String userName, String privilegeType, String database);

    public void grantUser(String userName, String roleName);

    public void revokeUser(String userName, String privilegeType, String database, String table, List<String> column);

    public void revokeUser(String userName, String privilegeType, String database, String table);

    public void revokeUser(String userName, String privilegeType, String database);

    public void revokeUser(String userName, String roleName);

    public void createRole(String roleName);

    public void removeRole(String roleName);

    public void grantRole(String roleName, String privilegeType, String database, String table, List<String> column);

    public void grantRole(String roleName, String privilegeType, String database, String table);

    public void grantRole(String roleName, String privilegeType, String database);

    public void revokeRole(String roleName, String privilegeType, String database, String table, List<String> column);

    public void revokeRole(String roleName, String privilegeType, String database, String table);

    public void revokeRole(String roleName, String privilegeType, String database);
}
