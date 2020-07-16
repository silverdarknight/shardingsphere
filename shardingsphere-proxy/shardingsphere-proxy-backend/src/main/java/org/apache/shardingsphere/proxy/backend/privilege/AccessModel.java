package org.apache.shardingsphere.proxy.backend.privilege;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeActionType;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlUserPrivilegeConfiguration;

import java.util.*;


@Getter
@Setter(value = AccessLevel.PRIVATE)
public class AccessModel implements AccessExecutorWrapper{

    private Map<String, UserInformation> userInformationMap = new HashMap<>();

    private Map<String, UserPrivilege> usersPrivilege = new HashMap<>();

    private Collection<String> invalidUserGroup = new HashSet<>();

    private Map<String, RolePrivilege> rolesPrivileges = new HashMap<>();

    public AccessModel(YamlAccessModel yamlAccessModel){
        // role privileges
        Iterator<Map.Entry<String, YamlPrivilegeConfiguration>> roleIterator =  yamlAccessModel.getRoleList().entrySet().iterator();
        while (roleIterator.hasNext()){
            Map.Entry<String, YamlPrivilegeConfiguration> kv = roleIterator.next();
            RolePrivilege tmpRolePrivilege = new RolePrivilege(kv.getKey());
            tmpRolePrivilege.constructModel(kv.getValue());
            this.addRole(tmpRolePrivilege);
        }
        Iterator<Map.Entry<String, YamlUserPrivilegeConfiguration>> userIterator =  yamlAccessModel.getUserList().entrySet().iterator();
        while (userIterator.hasNext()){
            Map.Entry<String, YamlUserPrivilegeConfiguration> kv = userIterator.next();
            YamlUserPrivilegeConfiguration curConfig = kv.getValue();
            String userName = kv.getKey();
            // user information
            UserInformation tmpUserInformation = this.addUser(userName,curConfig.getPassword());
            this.getUserInformationMap().put(userName,tmpUserInformation);
            try {
                // user privileges
                UserPrivilege tmpUserPrivilege = new UserPrivilege();
                Iterator<String> roleNamesIterator = curConfig.getRoles().iterator();
                while (roleNamesIterator.hasNext()){
                    String roleName = roleNamesIterator.next();
                    tmpUserPrivilege.grant(this.getRolePrivilege(roleName));
                }
                tmpUserPrivilege.constructModel(curConfig.getPrivileges());
                this.addUserPrivilege(tmpUserInformation, tmpUserPrivilege);
            }
            catch (Exception e){
                //
            }
        }
        // invalid group
        this.getInvalidUserGroup().addAll(yamlAccessModel.getInvalidGroup());
    }

    // search
    private Boolean containsUser(String userName){
        return userInformationMap.containsKey(userName);
    }

    private Boolean containsRole(String roleName){
        roleName = roleName.trim();
        return this.getRolesPrivileges().containsKey(roleName);
    }

    private UserInformation getUser(String userName){
        if(!this.getUserInformationMap().containsKey(userName))
            throw new ShardingSphereException("No such user named :" + userName);
        return this.getUserInformationMap().get(userName);
    }

    private UserPrivilege getUserPrivilege(String userName){
        UserInformation userInformation = this.getUser(userName);
        UserPrivilege userPrivilege = this.getUsersPrivilege().get(userInformation);
        if(userPrivilege == null)
            throw new ShardingSphereException("User named :" + userName + "has no privilege granted");
        else
            return userPrivilege;
    }

    private RolePrivilege getRolePrivilege(String roleName){
        roleName = roleName.trim();
        if(this.containsRole(roleName))
            return this.getRolesPrivileges().get(roleName);
        throw new ShardingSphereException("No such role named :" + roleName);
    }

    // add
    private UserInformation addUser(String userName, String password){
        if(this.containsUser(userName))
            throw new ShardingSphereException("Already has a user called : " + userName);
        UserInformation userInformation = new UserInformation(userName, password);
        this.getUserInformationMap().put(userName, userInformation);
        return userInformation;
    }

    private UserPrivilege addUserPrivilege(UserInformation userInformation, UserPrivilege userPrivilege){
        this.getUsersPrivilege().put(userInformation.getUserName(), userPrivilege);
        return userPrivilege;
    }

    private RolePrivilege addRole(RolePrivilege rolePrivilege){
        if(!this.getRolesPrivileges().containsKey(rolePrivilege.getRoleName())){
            this.getRolesPrivileges().put(rolePrivilege.getRoleName()
                    , rolePrivilege);
            return rolePrivilege;
        }
        throw new ShardingSphereException("Already has a role called : " + rolePrivilege.getRoleName());
    }

    @Override
    public void createUser(String byUserName, String userName, String password) {
        if(checkHavePermission(byUserName, PrivilegeAction.CREATE)){
            UserInformation information = new UserInformation(userName, password);
            if(!this.getUserInformationMap().containsKey(userName)){
                this.getUserInformationMap().put(userName, information);
            }
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void createRole(String byUserName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.CREATE)){
            RolePrivilege information = new RolePrivilege(roleName);
            if(!this.getRolesPrivileges().containsKey(roleName)){
                this.getRolesPrivileges().put(roleName, information);
            }
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void removeUser(String byUserName, String userName) {
        if(checkHavePermission(byUserName, PrivilegeAction.REMOVE)){
            getInvalidUserGroup().remove(userName);
            getUserInformationMap().remove(userName);
            getUsersPrivilege().remove(userName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void removeRole(String byUserName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.REMOVE)){
            RolePrivilege targetRole = getRolePrivilege(roleName);
            // users revoke role
            Iterator<Map.Entry<String, UserPrivilege>> userPrivilegeIterator = getUsersPrivilege()
                    .entrySet().iterator();
            while (userPrivilegeIterator.hasNext()){
                Map.Entry<String, UserPrivilege> kv = userPrivilegeIterator.next();
                try {
                    kv.getValue().revoke(targetRole);
                }
                catch (Exception e){
                    //
                }
            }
            // remove role
            getRolesPrivileges().remove(roleName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void disableUser(String byUserName, String userName) {
        if(checkHavePermission(byUserName, PrivilegeAction.DISABLE)){
            getInvalidUserGroup().add(userName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            Iterator<String> iterator = column.iterator();
            while (iterator.hasNext()){
                if(!checkUserPrivilege(byUserName,userName,privilegeType,database,table,iterator.next()))
                    return false;
            }
            return true;
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String database, String table, String column) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            return getUsersPrivilege().get(userName).checkPrivilege(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName, String userName, String privilegeType, String information, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            return getUsersPrivilege().get(userName).checkPrivilege(privilegeType,information,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            Iterator<String> iterator = column.iterator();
            while (iterator.hasNext()){
                if(!checkRolePrivilege(byUserName,roleName,privilegeType,database,table,iterator.next()))
                    return false;
            }
            return true;
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String database, String table, String column) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            return getRolesPrivileges().get(roleName).checkPrivilege(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkRolePrivilege(String byUserName, String roleName, String privilegeType, String information, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            return getRolesPrivileges().get(roleName).checkPrivilege(privilegeType,information,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getUsersPrivilege().get(userName).grant(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getUsersPrivilege().get(userName).grant(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getUsersPrivilege().get(userName).grant(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            RolePrivilege rolePrivilege = getRolePrivilege(roleName);
            getUsersPrivilege().get(userName).grant(rolePrivilege);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getRolesPrivileges().get(roleName).grant(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getRolesPrivileges().get(roleName).grant(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getRolesPrivileges().get(roleName).grant(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getUsersPrivilege().get(userName).revoke(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getUsersPrivilege().get(userName).revoke(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getUsersPrivilege().get(userName).revoke(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            RolePrivilege rolePrivilege = getRolePrivilege(roleName);
            getUsersPrivilege().get(userName).revoke(rolePrivilege);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getRolePrivilege(roleName).revoke(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getRolePrivilege(roleName).revoke(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getRolePrivilege(roleName).revoke(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    private Boolean checkHavePermission(String byUser, String actionType){
        if(getInvalidUserGroup().contains(byUser)) return false;
        return true;
    }
}
