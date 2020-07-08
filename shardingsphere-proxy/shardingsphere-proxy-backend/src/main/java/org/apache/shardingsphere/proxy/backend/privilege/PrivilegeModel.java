package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.HashSet;
import java.util.Objects;

/**
 * Privilege model.
 */
@Getter
@Setter
public class PrivilegeModel {
    public final static int INITIAL_PRIVILEGE_LENGTH = 8;
    // grant create delete(drop) update select
    protected HashSet<PrivilegePath> grantPrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , insertPrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , deletePrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , updatePrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , selectPrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH);

    protected HashSet<PrivilegePath> chosePrivilegeType(String privilegeType){
        switch (privilegeType){
            case "grant":
                return this.getGrantPrivilegePaths();
            case "create":
                return this.getInsertPrivilegePaths();
            case "delete":
                return this.getDeletePrivilegePaths();
            case "update":
                return this.getUpdatePrivilegePaths();
            case "select":
                return this.getSelectPrivilegePaths();
            default:
                throw new ShardingSphereException("Can not match privilege type");
        }
    }

    protected void addPrivilege(String privilegeType, PrivilegePath privilegePath){
        HashSet<PrivilegePath> targetPrivilegePaths = chosePrivilegeType(privilegeType);
        targetPrivilegePaths.add(privilegePath);
    }

    protected void removePrivilege(String privilegeType, PrivilegePath privilegePath){
        HashSet<PrivilegePath> targetPrivilegePaths = chosePrivilegeType(privilegeType);
        if(targetPrivilegePaths.contains(privilegePath)){
            targetPrivilegePaths.remove(privilegePath);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivilegeModel that = (PrivilegeModel) o;
        return Objects.equals(grantPrivilegePaths, that.grantPrivilegePaths) &&
                Objects.equals(insertPrivilegePaths, that.insertPrivilegePaths) &&
                Objects.equals(deletePrivilegePaths, that.deletePrivilegePaths) &&
                Objects.equals(updatePrivilegePaths, that.updatePrivilegePaths) &&
                Objects.equals(selectPrivilegePaths, that.selectPrivilegePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grantPrivilegePaths, insertPrivilegePaths, deletePrivilegePaths, updatePrivilegePaths, selectPrivilegePaths);
    }
}
