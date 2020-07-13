package org.apache.shardingsphere.proxy.backend.privilege;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.*;

@Getter
@Setter(value = AccessLevel.PRIVATE)
public class PrivilegePathTree {

    private PrivilegePathTreeNode root = new PrivilegePathTreeNode();

    protected Boolean checkPath(String dbName){
        if(root.getContainsStar()) return true;
        Iterator<PrivilegePathTreeNode> iteratorDB = root.getOffspring().iterator();
        while (iteratorDB.hasNext()){
            PrivilegePathTreeNode DBNode = iteratorDB.next();
            if(DBNode.isPath(dbName)) return true;
        }
        return false;
    }

    protected Boolean checkPath(String dbName, String tableName){
        if(root.getContainsStar()) return true;
        Iterator<PrivilegePathTreeNode> iteratorDB = root.getOffspring().iterator();
        while (iteratorDB.hasNext()){
            PrivilegePathTreeNode DBNode = iteratorDB.next();
            if(DBNode.isPath(dbName)){
                if(DBNode.getContainsStar()) return true;
                Iterator<PrivilegePathTreeNode> iteratorTable = DBNode.getOffspring().iterator();
                while (iteratorTable.hasNext()){
                    PrivilegePathTreeNode tableNode = iteratorTable.next();
                    if(tableNode.isPath(tableName)) return true;
                }
            }
        }
        return false;
    }

    protected Boolean checkPath(String dbName, String tableName, String colName){
        if(root.getContainsStar()) return true;
        Iterator<PrivilegePathTreeNode> iteratorDB = root.getOffspring().iterator();
        while (iteratorDB.hasNext()){
            PrivilegePathTreeNode DBNode = iteratorDB.next();
            if(DBNode.isPath(dbName)){
                if(DBNode.getContainsStar()) return true;
                Iterator<PrivilegePathTreeNode> iteratorTable = DBNode.getOffspring().iterator();
                while (iteratorTable.hasNext()){
                    PrivilegePathTreeNode tableNode = iteratorTable.next();
                    if(tableNode.isPath(tableName)) {
                        if(tableNode.getContainsStar()) return true;
                        Iterator<PrivilegePathTreeNode> iteratorCol = tableNode.getOffspring().iterator();
                        while (iteratorCol.hasNext()){
                            PrivilegePathTreeNode colNode = iteratorCol.next();
                            if(colNode.isPath(colName)) return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected void grantPath(String dbName){
        getRoot().addOffspring(dbName);
    }

    protected void grantPath(String dbName, String tableName){
        getRoot().addOffspring(dbName, tableName);
    }

    protected void grantPath(String dbName, String tableName, List<String> colNames){
        getRoot().addOffspring(dbName, tableName, colNames);
    }

    protected void revokePath(String dbName){
        getRoot().removeOffspring(dbName);
    }

    protected void revokePath(String dbName, String tableName){
        getRoot().removeOffspring(dbName, tableName);
    }

    protected void revokePath(String dbName, String tableName, List<String> colNames){
        getRoot().removeOffspring(dbName, tableName, colNames);
    }
}

