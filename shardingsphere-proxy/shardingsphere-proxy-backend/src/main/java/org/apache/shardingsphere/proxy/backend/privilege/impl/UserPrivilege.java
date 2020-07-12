package org.apache.shardingsphere.proxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegePath;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeExecutorWrapper;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeModel;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.*;

public class UserPrivilege extends PrivilegeModel implements PrivilegeExecutorWrapper {
    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private String password;

    public UserPrivilege(String userName, String password){
        this.setUserName(userName);
        this.setPassword(password);
    }

    private HashSet<RolePrivilege> roles = new HashSet<>();

    private void addRole(RolePrivilege role){
        this.roles.add(role);
    }

    private void removeRole(RolePrivilege role){
        this.roles.remove(role);
    }

    private HashSet<RolePrivilege> getRoles(){
        return this.roles;
    }

    public List<String> getRolesName(){
        List<String> rolesName = new LinkedList<>();
        Iterator<RolePrivilege> rolesIterator = this.getRoles().iterator();
        while (rolesIterator.hasNext()){
            rolesName.add(rolesIterator.next().getRoleName());
        }
        return rolesName;
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String information) {
        HashSet<PrivilegePath> targetPrivilegePaths = this.chosePrivilegeType(privilegeType);
        String[] splitTargets = information.split("\\.");
        switch (splitTargets.length){
            case 1:
                Iterator<PrivilegePath> iterator = targetPrivilegePaths.iterator();
                while (iterator.hasNext()){
                    PrivilegePath curPrivilegePath = iterator.next();
                    if(curPrivilegePath.containsTargetPlace(splitTargets[0])) return true;
                }
                break;
            case 2:
                if(checkPrivilege(privilegeType, splitTargets[0], splitTargets[1])) return true;
                break;
            case 3:
                if(checkPrivilege(privilegeType, splitTargets[0], splitTargets[1], splitTargets[2])) return true;
                break;
            default:
                throw new ShardingSphereException("Invalid privilege format.");
        }
        if(roles != null && roles.size()!=0){
            Iterator<RolePrivilege> roleIterator = roles.iterator();
            while (roleIterator.hasNext()){
                RolePrivilege curRolePrivilege = roleIterator.next();
                if(curRolePrivilege.checkPrivilege(privilegeType,information)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String database, String table) {
        HashSet<PrivilegePath> targetPrivilegePaths = this.chosePrivilegeType(privilegeType);
        Iterator<PrivilegePath> iterator = targetPrivilegePaths.iterator();
        while (iterator.hasNext()){
            PrivilegePath curPrivilegePath = iterator.next();
            if(curPrivilegePath.containsTargetPlace(database,table)) return true;
        }
        if(roles != null && roles.size()!=0){
            Iterator<RolePrivilege> roleIterator = roles.iterator();
            while (roleIterator.hasNext()){
                RolePrivilege curRolePrivilege = roleIterator.next();
                if(curRolePrivilege.checkPrivilege(privilegeType,database,table)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String database, String table, String column) {
        HashSet<PrivilegePath> targetPrivilegePaths = this.chosePrivilegeType(privilegeType);
        Iterator<PrivilegePath> iterator = targetPrivilegePaths.iterator();
        while (iterator.hasNext()){
            PrivilegePath curPrivilegePath = iterator.next();
            if(curPrivilegePath.containsTargetPlace(database,table,column)) return true;
        }
        if(roles != null && roles.size()!=0){
            Iterator<RolePrivilege> roleIterator = roles.iterator();
            while (roleIterator.hasNext()){
                RolePrivilege curRolePrivilege = roleIterator.next();
                if(curRolePrivilege.checkPrivilege(privilegeType,database,table,column)) return true;
            }
        }
        return false;
    }

    public void grant(RolePrivilege role){
        this.preGrant();
        this.addRole(role);
    }

    @Override
    public void grant(String privilegeType, String information) {
        this.preGrant();
        PrivilegePath targetPrivilegePath = new PrivilegePath(information);
        this.addPrivilege(privilegeType, targetPrivilegePath);
    }

    @Override
    public void grant(String privilegeType, String database, String table) {
        this.preGrant();
        PrivilegePath targetPrivilegePath = new PrivilegePath(database, table);
        this.addPrivilege(privilegeType, targetPrivilegePath);
    }

    @Override
    public void grant(String privilegeType, String database, String table, List<String> column) {
        PrivilegePath targetPrivilegePath = new PrivilegePath(database, table, column);
        this.addPrivilege(privilegeType, targetPrivilegePath);
    }

    public void revoke(RolePrivilege role){
        this.preRevoke();
        this.removeRole(role);
    }

    @Override
    public void revoke(String privilegeType, String information) {
        this.preRevoke();
        PrivilegePath privilegePath = new PrivilegePath(information);try{
            this.removePrivilege(privilegeType, privilegePath);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getUserName());
        }
    }

    @Override
    public void revoke(String privilegeType, String database, String table) {
        this.preRevoke();
        PrivilegePath privilegePath = new PrivilegePath(database, table);try{
            this.removePrivilege(privilegeType, privilegePath);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getUserName());
        }
    }

    @Override
    public void revoke(String privilegeType, String database, String table, List<String> column) {
        this.preRevoke();
        PrivilegePath privilegePath = new PrivilegePath(database, table, column);try{
            this.removePrivilege(privilegeType, privilegePath);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getUserName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserPrivilege that = (UserPrivilege) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(password, that.password) &&
                Objects.equals(roles, that.roles) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userName, password, roles);
    }
}
