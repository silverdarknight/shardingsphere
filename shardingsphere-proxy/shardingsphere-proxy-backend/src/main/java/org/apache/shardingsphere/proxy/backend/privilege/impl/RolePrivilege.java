package org.apache.shardingsphere.proxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegePath;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeModel;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class RolePrivilege extends PrivilegeModel {

    @Getter
    @Setter
    private String roleName;

    public RolePrivilege(String roleName){
        this.setRoleName(roleName);
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
                return false;
            case 2:
                return checkPrivilege(privilegeType, splitTargets[0], splitTargets[1]);
            case 3:
                return checkPrivilege(privilegeType, splitTargets[0], splitTargets[1], splitTargets[2]);
            default:
                throw new ShardingSphereException("Invalid privilege format.");
        }
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String database, String table) {
        HashSet<PrivilegePath> targetPrivilegePaths = this.chosePrivilegeType(privilegeType);
        Iterator<PrivilegePath> iterator = targetPrivilegePaths.iterator();
        while (iterator.hasNext()){
            PrivilegePath curPrivilegePath = iterator.next();
            if(curPrivilegePath.containsTargetPlace(database,table)) return true;
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
        return false;
    }


    @Override
    public void grant(String privilegeType, String information) {
        PrivilegePath targetPrivilegePath = new PrivilegePath(information);
        this.addPrivilege(privilegeType, targetPrivilegePath);
    }

    @Override
    public void grant(String privilegeType, String database, String table) {
        PrivilegePath targetPrivilegePath = new PrivilegePath(database, table);
        this.addPrivilege(privilegeType, targetPrivilegePath);
    }

    @Override
    public void grant(String privilegeType, String database, String table, List<String> column) {
        PrivilegePath targetPrivilegePath = new PrivilegePath(database, table, column);
        this.addPrivilege(privilegeType, targetPrivilegePath);
    }

    @Override
    public void revoke(String privilegeType, String information) {
        PrivilegePath privilegePath = new PrivilegePath(information);
        try{
            this.removePrivilege(privilegeType, privilegePath);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getRoleName());
        }
    }

    @Override
    public void revoke(String privilegeType, String database, String table) {
        PrivilegePath privilegePath = new PrivilegePath(database, table);
        try{
            this.removePrivilege(privilegeType, privilegePath);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getRoleName());
        }
    }

    @Override
    public void revoke(String privilegeType, String database, String table, List<String> column) {
        PrivilegePath privilegePath = new PrivilegePath(database, table, column);
        try{
            this.removePrivilege(privilegeType, privilegePath);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getRoleName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RolePrivilege that = (RolePrivilege) o;
        return Objects.equals(roleName, that.roleName) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roleName);
    }
}
