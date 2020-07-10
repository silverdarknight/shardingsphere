package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.Getter;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlUserPrivilegeConfiguration;

import java.util.*;

@Getter
public class AccessModel {
    public AccessModel(YamlAccessModel yamlAccessModel){
        // role
        Iterator<Map.Entry<String, YamlPrivilegeConfiguration>> roleIterator =  yamlAccessModel.getRoleList().entrySet().iterator();
        while (roleIterator.hasNext()){
            Map.Entry<String, YamlPrivilegeConfiguration> kv = roleIterator.next();
            RolePrivilege tmpRolePrivilege = new RolePrivilege(kv.getKey());
            tmpRolePrivilege.constructPrivileges(kv.getValue());
            this.addRole(tmpRolePrivilege);
        }
        //user
        Iterator<Map.Entry<String, YamlUserPrivilegeConfiguration>> userIterator =  yamlAccessModel.getUserList().entrySet().iterator();
        while (userIterator.hasNext()){
            Map.Entry<String, YamlUserPrivilegeConfiguration> kv = userIterator.next();
            YamlUserPrivilegeConfiguration curConfig = kv.getValue();
            UserPrivilege tmpUserPrivilege = new UserPrivilege(kv.getKey(),curConfig.getPassword());
            Iterator<String> roleNamesIterator = curConfig.getRoles().iterator();
            while (roleNamesIterator.hasNext()){
                String roleName = roleNamesIterator.next();
                tmpUserPrivilege.grant(this.getRole(roleName));
            }
            tmpUserPrivilege.constructPrivileges(curConfig.getPrivileges());
            this.addUser(tmpUserPrivilege);
        }
    }

    protected HashSet<UserPrivilege> usersPrivilege = new HashSet<>();

    protected HashSet<RolePrivilege> rolesPrivileges = new HashSet<>();

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

    public Boolean checkPrivilege(PrivilegeAction action){
        PrivilegeExecutorWrapper privilegeExecutorWrapper = action.isUser()?
                this.getUser(action.getName()):this.getRole(action.getName());
        return privilegeExecutorWrapper.checkPrivilege(action.getGrantActionType()
                , action.getPath().getPrivilegeInformation());
    }
}
