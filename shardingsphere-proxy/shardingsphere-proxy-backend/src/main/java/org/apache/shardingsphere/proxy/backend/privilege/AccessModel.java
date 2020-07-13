package org.apache.shardingsphere.proxy.backend.privilege;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Getter;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlUserPrivilegeConfiguration;

import java.util.*;


@Getter
public class AccessModel implements AccessExecutorWrapper{

    public final static String PRIVILEGE_TYPE_INSERT = "insert"
            , PRIVILEGE_TYPE_DELETE = "delete"
            , PRIVILEGE_TYPE_SELECT = "select"
            , PRIVILEGE_TYPE_UPDATE = "update";

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
            UserInformation tmpUserInformation = this.addUser(kv.getKey(),curConfig.getPassword());
            UserPrivilege tmpUserPrivilege = new UserPrivilege(tmpUserInformation);
            Iterator<String> roleNamesIterator = curConfig.getRoles().iterator();
            while (roleNamesIterator.hasNext()){
                String roleName = roleNamesIterator.next();
                tmpUserPrivilege.grant(this.getRolePrivilege(roleName));
            }
            tmpUserPrivilege.constructPrivileges(curConfig.getPrivileges());
            this.addUserPrivilege(tmpUserPrivilege);
        }
    }

    private Map<UserInformation, UserPrivilege> usersPrivilege = new HashMap<>();

    private Map<String, RolePrivilege> rolesPrivileges = new HashMap<>();

    private Collection<UserInformation> userInformation = new HashSet<>();

    private Collection<UserInformation> validUserGroup = new HashSet<>();

    // search
    public Boolean containsUser(String userName){
        userName = userName.trim();
        Iterator<UserInformation> iterator = this.getUserInformation().iterator();
        while (iterator.hasNext()){
            UserInformation curUserInformation = iterator.next();
            if(curUserInformation.getUserName().equals(userName)) return true;
        }
        return false;
    }

    public Boolean containsUserPrivilege(String userName){
        userName = userName.trim();
        UserPrivilege userPrivilege = this.getUserPrivilege(userName);
        if(userPrivilege == null)
            return false;
        else
            return true;
    }

    public Boolean containsRole(String roleName){
        roleName = roleName.trim();
        return this.getRolesPrivileges().containsKey(roleName);
    }

    public Boolean userAvailable(String userName){
        Iterator<UserInformation> userInformationIterator = validUserGroup.iterator();
        while (userInformationIterator.hasNext()){
            UserInformation userInformation = userInformationIterator.next();
            if(userInformation.getUserName().equals(userName)) return true;
        }
        return false;
    }

    public UserInformation getUser(String userName){
        Iterator<UserInformation> iterator = this.getUserInformation().iterator();
        while (iterator.hasNext()){
            UserInformation curUserInformation = iterator.next();
            if(curUserInformation.getUserName().equals(userName)) return curUserInformation;
        }
        throw new ShardingSphereException("No such user named :" + userName);
    }

    public Collection<UserInformation> getUser(List<String> userNames){
        Collection<UserInformation> userSet = new HashSet<>();
        Iterator<String> iterator = userNames.iterator();
        while (iterator.hasNext()){
            String curUserName = iterator.next();
            userSet.add(this.getUser(curUserName));
        }
        return userSet;
    }

    public UserPrivilege getUserPrivilege(String userName){
        userName = userName.trim();
        UserInformation userInformation = this.getUser(userName);
        UserPrivilege userPrivilege = this.getUsersPrivilege().get(userInformation);
        if(userPrivilege == null)
            throw new ShardingSphereException("User named :" + userName + "has no privilege granted");
        else
            return userPrivilege;
    }

    public Collection<UserPrivilege> getUserPrivileges(List<String> userNames){
        Collection<UserPrivilege> users = new HashSet<>();
        Iterator<String> iterator = userNames.iterator();
        while (iterator.hasNext()){
            String curUserName = iterator.next();
            users.add(this.getUserPrivilege(curUserName));
        }
        return users;
    }

    public RolePrivilege getRolePrivilege(String roleName){
        roleName = roleName.trim();
        if(this.containsRole(roleName))
            return this.getRolesPrivileges().get(roleName);
        throw new ShardingSphereException("No such role named :" + roleName);
    }

    public Collection<RolePrivilege> getRolePrivileges(List<String> roleNames){
        Collection<RolePrivilege> roles = new HashSet<>();
        Iterator<String> iterator = roleNames.iterator();
        while (iterator.hasNext()){
            String curRoleName = iterator.next();
            roles.add(this.getRolePrivilege(curRoleName));
        }
        return roles;
    }

    // add
    public UserInformation addUser(String userName, String password){
        if(this.containsUser(userName))
            throw new ShardingSphereException("Already has a user called : " + userName);
        UserInformation userInformation = new UserInformation(userName, password);
        this.getUserInformation().add(userInformation);
        return userInformation;
    }

    private UserPrivilege addUserPrivilege(UserPrivilege userPrivilege){
        this.getUsersPrivilege().put(userPrivilege.getUserInformation(),
                userPrivilege);
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

    // delete
    public void removeRole(RolePrivilege rolePrivilege){
        this.getRolesPrivileges().remove(rolePrivilege.getRoleName());
    }


    @Override
    public Boolean checkUserPrivilege(String userName, String privilegeType, String database, String table, List<String> column) {
        return null;
    }

    @Override
    public Boolean checkUserPrivilege(String userName, String privilegeType, String database, String table, String column) {
        return null;
    }

    @Override
    public Boolean checkUserPrivilege(String userName, String privilegeType, String database, String table) {
        return null;
    }

    @Override
    public Boolean checkUserPrivilege(String userName, String privilegeType, String database) {
        return null;
    }

    @Override
    public void createUser(String userName, String password) {

    }

    @Override
    public void removeUser(String userName) {

    }

    @Override
    public void grantUser(String userName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void grantUser(String userName, String privilegeType, String database, String table) {

    }

    @Override
    public void grantUser(String userName, String privilegeType, String database) {

    }

    @Override
    public void grantUser(String userName, String roleName) {

    }

    @Override
    public void revokeUser(String userName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void revokeUser(String userName, String privilegeType, String database, String table) {

    }

    @Override
    public void revokeUser(String userName, String privilegeType, String database) {

    }

    @Override
    public void revokeUser(String userName, String roleName) {

    }

    @Override
    public void createRole(String roleName) {

    }

    @Override
    public void removeRole(String roleName) {

    }

    @Override
    public void grantRole(String roleName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void grantRole(String roleName, String privilegeType, String database, String table) {

    }

    @Override
    public void grantRole(String roleName, String privilegeType, String database) {

    }

    @Override
    public void revokeRole(String roleName, String privilegeType, String database, String table, List<String> column) {

    }

    @Override
    public void revokeRole(String roleName, String privilegeType, String database, String table) {

    }

    @Override
    public void revokeRole(String roleName, String privilegeType, String database) {

    }
}
