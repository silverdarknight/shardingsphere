package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;

import java.util.LinkedList;
import java.util.List;

/**
 * byUser-create    -isUser-name+pw
 *                  -name
 *        remove    -isUser-name
 *                  -name
 *        disable   -isUser-name
 *                  -name
 *        revoke    -isUser-name-privilegeType-pathParameters
 *                  -name-privilegeType-pathParameters
 *        grant     -isUser-name-privilegeType-pathParameters
 *                  -name-privilegeType-pathParameters
 *
 *        check     -privilegeType-name-pathParameters
 */
@Getter(value = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PRIVATE)
public final class PrivilegeAction {

    public static String CREATE="create"
            ,REMOVE = "remove"
            ,DISABLE = "disable"
            ,REVOKE = "revoke"
            ,GRANT = "grant"
            ,CHECK = "check";

    // action by user
    private String actionUser = UserInformation.DEFAULT_USER;

    // action parameters (check user, grant revoke user/role)
    private String name;

    private Boolean isUser;

    private Boolean isCheckAction;

    private String grantActionType;

    private String dbName;

    private String tableName;

    private List<String> columns = new LinkedList<>();

    private Boolean privilegePathValid;

    // action parameters (add remove disable user/role)

    // action parameters (update userInfo) (not needed yet)

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

    public void asRole(){
        this.setIsUser(false);
    }

    public void asUser(){
        this.setIsUser(true);
    }

    public Boolean isUser(){
        return getIsUser();
    }

    private String[] splitInformation(String information){
        String[] dbAndTable = information.split("\\.");
        return dbAndTable;
    }
}
