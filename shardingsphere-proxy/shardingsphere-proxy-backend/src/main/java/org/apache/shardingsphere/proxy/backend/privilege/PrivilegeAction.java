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

    private String name;

    private Boolean isUser;

    private Boolean isCheckAction;

    private String grantActionType;

    private String dbName;

    private String tableName;

    private List<String> columns = new LinkedList<>();

    private Boolean privilegePathValid;

    public String[] splitInformation(String information){
        String[] dbAndTable = information.split("\\.");
        return dbAndTable;
    }
}
