package org.apache.shardingsphere.proxy.backend.privilege.common;

public enum PrivilegeActionType {

    INSERT, DELETE, SELECT, UPDATE,UNKNOWN_TYPE;

    public static PrivilegeActionType checkActionType(String inputActionType){
        switch (inputActionType.trim().toLowerCase()){
            case "insert":
                return INSERT;
            case "delete":
                return DELETE;
            case "select":
                return SELECT;
            case "update":
                return UPDATE;
            default:
                return UNKNOWN_TYPE;
        }
    }
}
