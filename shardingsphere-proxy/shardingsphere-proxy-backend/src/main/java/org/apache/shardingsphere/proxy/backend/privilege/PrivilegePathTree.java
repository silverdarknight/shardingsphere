package org.apache.shardingsphere.proxy.backend.privilege;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.io.Serializable;
import java.util.*;

@Getter(value = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
public class PrivilegePathTree implements Serializable {

    private PrivilegePathTreeNode root = new PrivilegePathTreeNode();

    public Boolean checkPath(String dbName, String tableName){
        if(root.getContainsStar()) return true;
        Iterator<PrivilegePathTreeNode> iteratorDB = root.getOffspring().iterator();
        while (iteratorDB.hasNext()){
            PrivilegePathTreeNode DBNode = iteratorDB.next();
            if(DBNode.isPath(dbName)){
                if(DBNode.getContainsStar()) return true;
                Iterator<PrivilegePathTreeNode> iteratorTable = DBNode.getOffspring().iterator();
                while (iteratorTable.hasNext()){
                    PrivilegePathTreeNode tableNode = iteratorTable.next();
                    if(tableNode.isPath(tableName) && tableNode.getContainsStar()) return true;
                }
            }
        }
        return false;
    }

    public Boolean checkPath(String dbName, String tableName, String colName){
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

    public void grantPath(String dbName, String tableName){
        getRoot().addOffspring(dbName, tableName);
    }

    public void grantPath(String dbName, String tableName, List<String> colNames){
        if(colNames.size()==0)
            getRoot().addOffspring(dbName, tableName);
        else
            getRoot().addOffspring(dbName, tableName, colNames);
    }

    public void revokePath(String dbName, String tableName){
        getRoot().removeOffspring(dbName, tableName);
        clearPath(dbName);
    }

    public void revokePath(String dbName, String tableName, List<String> colNames){
        getRoot().removeOffspring(dbName, tableName, colNames);
        clearPath(dbName, tableName);
        clearPath(dbName);
    }

    // if cols are null && do not have *, remove table node
    private void clearPath(String dbName, String tableName){
        dbName = dbName.trim();
        tableName = tableName.trim();
        Iterator<PrivilegePathTreeNode> dbIterator = this.getRoot().getOffspring().iterator();
        while (dbIterator.hasNext()){
            PrivilegePathTreeNode curNode = dbIterator.next();
            if(curNode.getPathValue().equals(dbName)){
                // clear table node
                Iterator<PrivilegePathTreeNode> tableIterator = curNode.getOffspring().iterator();
                while (tableIterator.hasNext()){
                    PrivilegePathTreeNode tableNode = tableIterator.next();
                    if(tableNode.getPathValue().equals(tableName) &&
                            tableNode.getOffspring().size()==0 &&
                            !tableNode.getContainsStar()){
                        curNode.getOffspring().remove(tableNode);
                        break;
                    }
                }
                // clear db node
                if(curNode.getOffspring().size()==0 && !curNode.getContainsStar()){
                    this.getRoot().getOffspring().remove(curNode);
                }
            }
        }
    }

    // if tables are null && do not have *, remove db node
    private void clearPath(String dbName){
        Iterator<PrivilegePathTreeNode> iterator = this.getRoot().getOffspring().iterator();
        while (iterator.hasNext()){
            PrivilegePathTreeNode curNode = iterator.next();
            if(curNode.getPathValue().equals(dbName) &&
                    curNode.getOffspring().size()==0 &&
                    !curNode.getContainsStar()){
                this.getRoot().getOffspring().remove(curNode);
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivilegePathTree that = (PrivilegePathTree) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}

