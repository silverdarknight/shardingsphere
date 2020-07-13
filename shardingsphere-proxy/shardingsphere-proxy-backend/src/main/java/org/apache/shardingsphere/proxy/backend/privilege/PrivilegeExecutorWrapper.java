package org.apache.shardingsphere.proxy.backend.privilege;

import java.util.List;

public interface PrivilegeExecutorWrapper {

    public boolean checkUserPrivilege(String userName, String privilegeType, String database, String table, List<String> column);

    public boolean checkUserPrivilege(String userName, String privilegeType, String database, String table, String column);

    public boolean checkUserPrivilege(String userName, String privilegeType, String database, String table);

    public boolean checkUserPrivilege(String userName, String privilegeType, String database);

    public void createUser(String userName, String password);

    public void removeUser(String userName);

    public void createRole(String roleName);

    public void removeRole(String roleName);

    public void disableUser(String userName);

    public void setUserPassword(String userName, String password);

    public void grantRole(String roleName, String privilegeType, String database, String table, List<String> column);

    public void grantRole(String roleName, String privilegeType, String database, String table);

    public void grantRole(String roleName, String privilegeType, String database);

    public void revokeRole(String roleName, String privilegeType, String database, String table, List<String> column);

    public void revokeRole(String roleName, String privilegeType, String database, String table);

    public void revokeRole(String roleName, String privilegeType, String database);

    public void grantUser(String userName, String privilegeType, String database, String table, List<String> column);

    public void grantUser(String userName, String privilegeType, String database, String table);

    public void grantUser(String userName, String privilegeType, String database);

    public void revokeUser(String userName, String privilegeType, String database, String table, List<String> column);

    public void revokeUser(String userName, String privilegeType, String database, String table);

    public void revokeUser(String userName, String privilegeType, String database);
}
