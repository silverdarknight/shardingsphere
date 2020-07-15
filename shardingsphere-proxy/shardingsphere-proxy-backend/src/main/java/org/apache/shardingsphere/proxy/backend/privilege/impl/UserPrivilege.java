package org.apache.shardingsphere.proxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlUserPrivilegeConfiguration;

import java.util.*;


@Getter
@Setter
public class UserPrivilege extends PrivilegeModel {

    private UserInformation userInformation;

    private HashSet<RolePrivilege> roles = new HashSet<>();

    public UserPrivilege(UserInformation userInformation){
        this.setUserInformation(userInformation);
    }

    public List<String> getRolesName(){
        List<String> rolesName = new LinkedList<>();
        Iterator<RolePrivilege> rolesIterator = this.getRoles().iterator();
        while (rolesIterator.hasNext()){
            rolesName.add(rolesIterator.next().getRoleName());
        }
        return rolesName;
    }

    public void grant(RolePrivilege role){
        this.getRoles().add(role);
    }

    public void revoke(RolePrivilege role){
        this.getRoles().remove(role);
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String dbName, String tableName, String column) {
        Iterator<RolePrivilege> iterator = this.getRoles().iterator();
        while (iterator.hasNext()){
            RolePrivilege curRole = iterator.next();
            if(curRole.checkPrivilege(privilegeType, dbName, tableName, column)) return true;
        }
        return super.checkPrivilege(privilegeType, dbName, tableName, column);
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String dbName, String tableName) {
        Iterator<RolePrivilege> iterator = this.getRoles().iterator();
        while (iterator.hasNext()){
            RolePrivilege curRole = iterator.next();
            if(curRole.checkPrivilege(privilegeType, dbName, tableName)) return true;
        }
        return super.checkPrivilege(privilegeType, dbName, tableName);
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String information) {
        Iterator<RolePrivilege> iterator = this.getRoles().iterator();
        while (iterator.hasNext()){
            RolePrivilege curRole = iterator.next();
            if(curRole.checkPrivilege(privilegeType, information)) return true;
        }
        return super.checkPrivilege(privilegeType, information);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserPrivilege that = (UserPrivilege) o;
        return Objects.equals(this.getUserInformation(), that.getUserInformation()) &&
                Objects.equals(roles, that.roles) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getUserInformation(), roles);
    }
}
