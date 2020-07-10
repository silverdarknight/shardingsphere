package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class PrivilegeAction {

    public final static String PRIVILEGE_TYPE_INSERT = "insert"
            , PRIVILEGE_TYPE_DELETE = "delete"
            , PRIVILEGE_TYPE_SELECT = "select"
            , PRIVILEGE_TYPE_UPDATE = "update";

    @Getter
    @Setter
    private String name;

    @Setter
    private Boolean isUser;

    @Getter
    @Setter
    private String grantActionType;

    @Getter
    @Setter
    private PrivilegePath path;

    private Boolean privilegePathValid = true;

    public PrivilegeAction(String userName, String actionType, String information){
        this.setName(userName);
        this.setGrantActionType(actionType.toLowerCase());
        try {
            this.setPath(new PrivilegePath(information));
        }
        catch (Exception e){
            this.privilegePathValid = false;
        }
    }

    public PrivilegeAction(String userName, String actionType, String database, String table){
        this.setName(userName);
        this.setGrantActionType(actionType.toLowerCase());
        try {
            this.setPath(new PrivilegePath(database, table));
        }
        catch (Exception e){
            this.privilegePathValid = false;
        }
    }

    public PrivilegeAction(String userName, String actionType, String database, String table, List<String> cols){
        this.setName(userName);
        this.setGrantActionType(actionType.toLowerCase());
        try {
            this.setPath(new PrivilegePath(database, table, cols));
        }
        catch (Exception e){
            this.privilegePathValid = false;
        }
    }

    public Boolean typeIsValid(){
        Boolean actionTypeValid = this.getGrantActionType().toLowerCase().equals(PrivilegeAction.PRIVILEGE_TYPE_DELETE)
                || this.getGrantActionType().toLowerCase().equals(PrivilegeAction.PRIVILEGE_TYPE_INSERT)
                || this.getGrantActionType().toLowerCase().equals(PrivilegeAction.PRIVILEGE_TYPE_SELECT)
                || this.getGrantActionType().toLowerCase().equals(PrivilegeAction.PRIVILEGE_TYPE_UPDATE);
        return actionTypeValid;
    }

    public Boolean isUser(){
        return this.isUser;
    }
}
