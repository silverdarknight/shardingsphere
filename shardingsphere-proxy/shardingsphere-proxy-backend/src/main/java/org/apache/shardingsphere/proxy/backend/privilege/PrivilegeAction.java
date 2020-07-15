package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.LinkedList;
import java.util.List;


@Getter
public final class PrivilegeAction {

    @Setter
    private String name;

    @Setter
    private Boolean isUser;

    @Getter
    @Setter
    private String grantActionType;

    @Getter
    @Setter
    private String dbName;

    @Getter
    @Setter
    private String tableName;

    @Getter
    @Setter
    private List<String> columns = new LinkedList<>();

    private Boolean privilegePathValid = true;

    public PrivilegeAction(String userName, String actionType, String dbName, String table){
        this.setName(userName);
        this.setGrantActionType(actionType.toLowerCase());
        try {
            setDbName(dbName);
            setTableName(table);
        }
        catch (Exception e){
            this.privilegePathValid = false;
        }
    }

    public PrivilegeAction(String userName, String actionType, String dbName, String table, List<String> cols){
        this.setName(userName);
        this.setGrantActionType(actionType.toLowerCase());
        try {
            setDbName(dbName);
            setTableName(table);
            setColumns(cols);
        }
        catch (Exception e){
            this.privilegePathValid = false;
        }
    }

    public PrivilegeAction(String userName, String actionType, String dbName, String table, String col){
        this.setName(userName);
        this.setGrantActionType(actionType.toLowerCase());
        try {
            setDbName(dbName);
            setTableName(table);
            getColumns().add(col);
        }
        catch (Exception e){
            this.privilegePathValid = false;
        }
    }

    public Boolean isUser(){
        return this.isUser;
    }

    private String[] splitInformation(String information){
        String[] dbAndTable = information.split("\\.");
        return dbAndTable;
    }
}
