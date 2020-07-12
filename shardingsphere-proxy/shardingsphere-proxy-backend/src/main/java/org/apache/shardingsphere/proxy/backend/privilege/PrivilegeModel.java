package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;

import java.util.*;

/**
 * Privilege model.
 */
@Getter
@Setter
public class PrivilegeModel {

    public final static int INITIAL_PRIVILEGE_LENGTH = 8;

    protected Boolean status = true;

    public void disable(){
        this.status = false;
    }

    public void available(){
        this.status = true;
    }

    public Boolean isUsable(){
        return this.status;
    }

    protected void preGrant(){
        if(!isUsable()) throw new ShardingSphereException("This is not allowed to use");
    }

    protected void preRevoke(){
        if(!isUsable()) throw new ShardingSphereException("This is not allowed to use");
    }

    // grant create delete(drop) update select
    protected HashSet<PrivilegePath> grantPrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , insertPrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , deletePrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , updatePrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            , selectPrivilegePaths = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH);

    protected void constructPrivileges(YamlPrivilegeConfiguration yamlPrivilegeConfiguration){
        // insert
        Iterator<String> iterator = yamlPrivilegeConfiguration.getInsert().iterator();
        while (iterator.hasNext()){
            String curInformation = iterator.next();
            this.addPrivilege(PrivilegeAction.PRIVILEGE_TYPE_INSERT,curInformation);
        }
        // delete
        iterator = yamlPrivilegeConfiguration.getDelete().iterator();
        while (iterator.hasNext()){
            String curInformation = iterator.next();
            this.addPrivilege(PrivilegeAction.PRIVILEGE_TYPE_DELETE,curInformation);
        }
        // select
        iterator = yamlPrivilegeConfiguration.getSelect().iterator();
        while (iterator.hasNext()){
            String curInformation = iterator.next();
            this.addPrivilege(PrivilegeAction.PRIVILEGE_TYPE_SELECT,curInformation);
        }
        // update
        iterator = yamlPrivilegeConfiguration.getUpdate().iterator();
        while (iterator.hasNext()){
            String curInformation = iterator.next();
            this.addPrivilege(PrivilegeAction.PRIVILEGE_TYPE_UPDATE,curInformation);
        }
    }

    protected HashSet<PrivilegePath> chosePrivilegeType(String privilegeType){
        switch (privilegeType){
            case "grant":
                return this.getGrantPrivilegePaths();
            case PrivilegeAction.PRIVILEGE_TYPE_INSERT:
                return this.getInsertPrivilegePaths();
            case PrivilegeAction.PRIVILEGE_TYPE_DELETE:
                return this.getDeletePrivilegePaths();
            case PrivilegeAction.PRIVILEGE_TYPE_UPDATE:
                return this.getUpdatePrivilegePaths();
            case PrivilegeAction.PRIVILEGE_TYPE_SELECT:
                return this.getSelectPrivilegePaths();
            default:
                throw new ShardingSphereException("Can not match privilege type");
        }
    }

    protected void addPrivilege(String privilegeType, PrivilegePath privilegePath){
        HashSet<PrivilegePath> targetPrivilegePaths = chosePrivilegeType(privilegeType);
        targetPrivilegePaths.add(privilegePath);
    }

    protected void addPrivilege(String privilegeType, String information){
        HashSet<PrivilegePath> targetPrivilegePaths = chosePrivilegeType(privilegeType);
        targetPrivilegePaths.add(new PrivilegePath(information));
    }

    protected void addPrivileges(String privilegeType, List<String> informationList){
        HashSet<PrivilegePath> targetPrivilegePaths = chosePrivilegeType(privilegeType);
        targetPrivilegePaths.addAll(PrivilegePath.constructor(informationList));
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
