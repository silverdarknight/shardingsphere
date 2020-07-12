package org.apache.shardingsphere.proxy.backend.privilege;

import java.util.List;

public class PrivilegeExecutor {

    private AccessModel accessModel;

    private ZKPrivilegeWatcher zkPrivilegeWatcher;

    public boolean check(PrivilegeAction action){
        return true;
    }

    public void grant(PrivilegeAction action){
        // add user privilege
    }

    public void revoke(PrivilegeAction action){
        // remove user privilege
    }

    public void addUser(String userName, String password){
        // add user
    }

    public void removeUser(String userName){
        // remove user
    }

    public void addRole(String roleName){
        // add role
    }

    public void removeRole(String roleName){
        // remove role
    }
}
