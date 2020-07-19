package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeActionType;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegePath;

import java.io.Serializable;
import java.util.*;

/**
 * Privilege model.
 */
@Getter
@Setter
public abstract class PrivilegeModel implements Serializable {

    public final static int INITIAL_PRIVILEGE_LENGTH = 8;

    protected Map<PrivilegeActionType, PrivilegePathTree> privilegePaths = new HashMap<>(PrivilegeActionType.values().length);

    public PrivilegeModel(){
        EnumSet<PrivilegeActionType> actionTypes = EnumSet.allOf(PrivilegeActionType.class);
        Iterator<PrivilegeActionType> iterator = actionTypes.iterator();
        while (iterator.hasNext()){
            PrivilegeActionType curActionType = iterator.next();
            if(curActionType != PrivilegeActionType.UNKNOWN_TYPE)
                getPrivilegePaths().put(curActionType, new PrivilegePathTree());
        }
    }


    public void constructModel(YamlPrivilegeConfiguration yamlPrivilegeConfiguration){
        // insert
        Iterator<YamlPrivilegePath> iterator = yamlPrivilegeConfiguration.getInsert().iterator();
        while (iterator.hasNext()){
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if(yamlPrivilegePath.getCols()!=null && yamlPrivilegePath.getCols().size()!=0)
                this.grant("insert",
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            else
                this.grant("insert",
                        yamlPrivilegePath.getInformation());
        }
        // delete
        iterator = yamlPrivilegeConfiguration.getDelete().iterator();
        while (iterator.hasNext()){
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if(yamlPrivilegePath.getCols()!=null && yamlPrivilegePath.getCols().size()!=0)
                this.grant("delete",
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            else
                this.grant("insert",
                        yamlPrivilegePath.getInformation());
        }
        // select
        iterator = yamlPrivilegeConfiguration.getSelect().iterator();
        while (iterator.hasNext()){
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if(yamlPrivilegePath.getCols()!=null && yamlPrivilegePath.getCols().size()!=0)
                this.grant("select",
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            else
                this.grant("select",
                        yamlPrivilegePath.getInformation());
        }
        // update
        iterator = yamlPrivilegeConfiguration.getUpdate().iterator();
        while (iterator.hasNext()){
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if(yamlPrivilegePath.getCols()!=null && yamlPrivilegePath.getCols().size()!=0)
                this.grant("update",
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            else
                this.grant("update",
                        yamlPrivilegePath.getInformation());
        }
    }

    protected PrivilegePathTree chosePrivilegeType(String privilegeType){
        PrivilegeActionType actionType = PrivilegeActionType.checkActionType(privilegeType);
        if(actionType == PrivilegeActionType.UNKNOWN_TYPE)
            throw new ShardingSphereException("Can not match privilege type");
        else return getPrivilegePaths().get(actionType);
    }

    protected void grant(String privilegeType, String dbName, String tableName, List<String> cols){
        PrivilegePathTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.grantPath(dbName, tableName, cols);
    }

    protected void grant(String privilegeType, String dbName, String tableName){
        PrivilegePathTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.grantPath(dbName, tableName);
    }

    protected void grant(String privilegeType, String information){
        String[] splitInfo = splitInformation(information);
        if(splitInfo.length==1){
            throw new ShardingSphereException("illegal input target database and table");
        }
        else if(splitInfo.length==2){
            grant(privilegeType, splitInfo[0], splitInfo[1]);
        }
        else throw new ShardingSphereException("illegal input target database and table");
    }

    protected void grant(String privilegeType, String information, List<String> cols){
        String[] splitInfo = splitInformation(information);
        if(splitInfo.length==1){
            throw new ShardingSphereException("illegal input target database and table");
        }
        else if(splitInfo.length==2){
            grant(privilegeType, splitInfo[0], splitInfo[1], cols);
        }
        else throw new ShardingSphereException("illegal input target database and table");
    }

    protected void revoke(String privilegeType, String dbName, String tableName, List<String> cols){
        PrivilegePathTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.revokePath(dbName, tableName,cols);
    }

    protected void revoke(String privilegeType, String dbName, String tableName){
        PrivilegePathTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.revokePath(dbName, tableName);
    }

    protected void revoke(String privilegeType, String information){
        String[] splitInfo = splitInformation(information);
        if(splitInfo.length==1){
            revoke(privilegeType, splitInfo[0]);
        }
        else if(splitInfo.length==2){
            revoke(privilegeType, splitInfo[0], splitInfo[1]);
        }
        else throw new ShardingSphereException("illegal input target database and table");
    }

    protected void revoke(String privilegeType, String information, List<String> cols){
        String[] splitInfo = splitInformation(information);
        if(splitInfo.length==1){
            throw new ShardingSphereException("illegal input target database and table");
        }
        else if(splitInfo.length==2){
            revoke(privilegeType, splitInfo[0], splitInfo[1], cols);
        }
        else throw new ShardingSphereException("illegal input target database and table");
    }

    public boolean checkPrivilege(String privilegeType, String dbName, String tableName, String column){
        PrivilegePathTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        return targetPrivilegeTree.checkPath(dbName, tableName, column);
    }

    public boolean checkPrivilege(String privilegeType, String dbName, String tableName){
        PrivilegePathTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        return targetPrivilegeTree.checkPath(dbName, tableName);
    }

    public boolean checkPrivilege(String privilegeType, String information){
        String[] splitInfo = splitInformation(information);
        if(splitInfo.length==1){
            throw new ShardingSphereException("illegal input target database and table");
        }
        else if(splitInfo.length==2){
            return checkPrivilege(privilegeType, splitInfo[0], splitInfo[1]);
        }
        else {
            return checkPrivilege(privilegeType, splitInfo[0], splitInfo[1], splitInfo[2]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivilegeModel that = (PrivilegeModel) o;
        return Objects.equals(privilegePaths, that.privilegePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privilegePaths);
    }

    private String[] splitInformation(String information){
        String[] dbAndTable = information.split("\\.");
        return dbAndTable;
    }
}
