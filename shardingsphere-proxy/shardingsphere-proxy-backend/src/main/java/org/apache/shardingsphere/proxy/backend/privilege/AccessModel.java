package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.Getter;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Getter
public class AccessModel {
    private HashSet<UserPrivilege> usersPrivilege = new HashSet<>();

    private HashSet<RolePrivilege> rolesPrivileges = new HashSet<>();

    public void addUser(UserPrivilege userPrivilege){
        this.getUsersPrivilege().add(userPrivilege);
    }

    public void removeUser(UserPrivilege userPrivilege){
        this.getUsersPrivilege().remove(userPrivilege);
    }

    public void addRole(RolePrivilege rolePrivilege){
        this.getRolesPrivileges().add(rolePrivilege);
    }

    public void removeUser(RolePrivilege rolePrivilege){
        this.getRolesPrivileges().remove(rolePrivilege);
    }

    public UserPrivilege getUser(String userName){
        Iterator<UserPrivilege> iterator = this.getUsersPrivilege().iterator();
        while (iterator.hasNext()){
            UserPrivilege curUserPrivilege = iterator.next();
            if(curUserPrivilege.getUserName().equals(userName)) return curUserPrivilege;
        }
        throw new ShardingSphereException("No such user named :" + userName);
    }

    public Collection<UserPrivilege> getUsers(List<String> userNames){
        Collection<UserPrivilege> users = new HashSet<>();
        Iterator<String> iterator = userNames.iterator();
        while (iterator.hasNext()){
            String curUserName = iterator.next();
            users.add(this.getUser(curUserName));
        }
        return users;
    }

    public RolePrivilege getRole(String roleName){
        Iterator<RolePrivilege> iterator = this.getRolesPrivileges().iterator();
        while (iterator.hasNext()){
            RolePrivilege curRolePrivilege = iterator.next();
            if(curRolePrivilege.getRoleName().equals(roleName)) return curRolePrivilege;
        }
        throw new ShardingSphereException("No such role named :" + roleName);
    }

    public Collection<RolePrivilege> getRoles(List<String> roleNames){
        Collection<RolePrivilege> roles = new HashSet<>();
        Iterator<String> iterator = roleNames.iterator();
        while (iterator.hasNext()){
            String curRoleName = iterator.next();
            roles.add(this.getRole(curRoleName));
        }
        return roles;
    }
}
