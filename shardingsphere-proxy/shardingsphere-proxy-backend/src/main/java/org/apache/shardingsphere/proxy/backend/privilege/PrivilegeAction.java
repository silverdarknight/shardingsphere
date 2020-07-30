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
 *                              -roleName
 *                  -name-privilegeType-pathParameters
 *        grant     -isUser-name-privilegeType-pathParameters
 *                              -roleName
 *                  -name-privilegeType-pathParameters
 *
 *        check     -privilegeType-name-pathParameters
 */
@Getter
@Setter
public final class PrivilegeAction {

    public static String CREATE="create"
            ,REMOVE = "remove"
            ,DISABLE = "disable"
            ,REVOKE = "revoke"
            ,GRANT = "grant"
            ,CHECK = "check";

    // action by user
    private String byUser = UserInformation.DEFAULT_USER;

    private String actionType;

    private Boolean isUser;

    private String name;

    private String password;

    private String privilegeType;

    private String dbName;

    private String tableName;

    private List<String> columns = new LinkedList<>();

    private String roleName;

    private Boolean privilegePathValid;

    public String[] splitInformationByDot(String information){
        String[] dbAndTable = information.split("\\.");
        return dbAndTable;
    }

    public static PrivilegeAction addUser(String byUser, String name, String pw){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.CREATE);
        action.setIsUser(true);
        action.setName(name);
        action.setPassword(pw);
        return action;
    }

    public static PrivilegeAction addRole(String byUser, String name){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.CREATE);
        action.setIsUser(false);
        action.setName(name);
        return action;
    }

    public static PrivilegeAction removeUser(String byUser, String name){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.REMOVE);
        action.setIsUser(true);
        action.setName(name);
        return action;
    }

    public static PrivilegeAction removeRole(String byUser, String name){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.REMOVE);
        action.setIsUser(false);
        action.setName(name);
        return action;
    }

    public static PrivilegeAction disableUser(String byUser, String name){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.DISABLE);
        action.setIsUser(true);
        action.setName(name);
        return action;
    }

    public static PrivilegeAction checkPrivilege(String byUser, String name,
                                                 String privilegeType,
                                                 String dbName,
                                                 String tableName,
                                                 List<String> cols){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.CHECK);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if(cols.size()==0) action.setColumns(null);
        else action.setColumns(cols);
        return action;
    }

    public static PrivilegeAction grantUserPrivilege(String byUser, String name,
                                                 String privilegeType,
                                                 String dbName,
                                                 String tableName,
                                                 List<String> cols){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.GRANT);
        action.setIsUser(true);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if(cols.size()==0) action.setColumns(null);
        else action.setColumns(cols);
        return action;
    }

    public static PrivilegeAction grantUserRole(String byUser,
                                                String name,
                                                String roleName){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.GRANT);
        action.setIsUser(true);
        action.setName(name);
        action.setRoleName(roleName);
        return action;
    }

    public static PrivilegeAction grantRolePrivilege(String byUser, String name,
                                                     String privilegeType,
                                                     String dbName,
                                                     String tableName,
                                                     List<String> cols){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.GRANT);
        action.setIsUser(false);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if(cols.size()==0) action.setColumns(null);
        else action.setColumns(cols);
        return action;
    }

    public static PrivilegeAction revokeUserPrivilege(String byUser, String name,
                                                     String privilegeType,
                                                     String dbName,
                                                     String tableName,
                                                     List<String> cols){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.REVOKE);
        action.setIsUser(true);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if(cols.size()==0) action.setColumns(null);
        else action.setColumns(cols);
        return action;
    }

    public static PrivilegeAction revokeUserRole(String byUser,
                                                String name,
                                                String roleName){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.REVOKE);
        action.setIsUser(true);
        action.setName(name);
        action.setRoleName(roleName);
        return action;
    }

    public static PrivilegeAction revokeRolePrivilege(String byUser, String name,
                                                     String privilegeType,
                                                     String dbName,
                                                     String tableName,
                                                     List<String> cols){
        PrivilegeAction action = new PrivilegeAction();
        action.setByUser(byUser);
        action.setActionType(PrivilegeAction.REVOKE);
        action.setIsUser(false);
        action.setName(name);
        action.setPrivilegeType(privilegeType);
        action.setDbName(dbName);
        action.setTableName(tableName);
        if(cols.size()==0) action.setColumns(null);
        else action.setColumns(cols);
        return action;
    }
}
