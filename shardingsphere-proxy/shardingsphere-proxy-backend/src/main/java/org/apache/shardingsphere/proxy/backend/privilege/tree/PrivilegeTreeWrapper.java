package org.apache.shardingsphere.proxy.backend.privilege.tree;

import java.util.List;

public interface PrivilegeTreeWrapper {
    public Boolean checkPath(String dbName, String tableName);

    public Boolean checkPath(String dbName, String tableName, String colName);

    public void grantPath(String dbName, String tableName);

    public void grantPath(String dbName, String tableName, List<String> colNames);

    public void grantPath(String dbName, String tableName, String colNames);

    public void revokePath(String dbName, String tableName);

    public void revokePath(String dbName, String tableName, List<String> colNames);

    public void revokePath(String dbName, String tableName, String colNames);
}
