/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.privilege.model.tree;

import lombok.Getter;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeExceptions;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Getter
public class Tree implements Serializable {

    private static final long serialVersionUID = -9025534510942972272L;

    private final Node root = new Node("root");

    /**
     * check node have path.
     *
     * @param dbName database
     * @param tableName table
     * @return have path
     */
    public Boolean checkPath(final String dbName, final String tableName) {
        if (root.hasStar()) {
            return true;
        }
        try {
            Node dbNode = root.getChild(dbName);
            if (dbNode.hasStar()) {
                return true;
            } else {
                Node tableNode = dbNode.getChild(tableName);
                return tableNode.hasStar();
            }
        } catch (NullPointerException ex) {
            return false;
        }
    }

    /**
     * check node have path.
     *
     * @param dbName database
     * @param tableName table
     * @param colName column
     * @return have path
     */
    public Boolean checkPath(final String dbName, final String tableName, final String colName) {
        if (root.hasStar()) {
            return true;
        }
        try {
            Node dbNode = root.getChild(dbName);
            if (dbNode.hasStar()) {
                return true;
            }
            Node tableNode = dbNode.getChild(tableName);
            if (tableNode.hasStar()) {
                return true;
            } else {
                Node columnNode = tableNode.getChild(colName);
                return columnNode.hasStar();
            }
        } catch (NullPointerException ex) {
            return false;
        }
    }

    /**
     * grant db.table.
     *
     * @param dbName database
     * @param tableName table
     */
    public void grantPath(final String dbName, final String tableName) {
        Boolean dbSuccess = root.addChild(dbName);
        if (!"*".equals(dbName.trim())) {
            Node dbNode = root.getChild(dbName);
            Boolean tableSucc = dbNode.addChild(tableName);
            if (!tableSucc && ("*".equals(tableName) || dbNode.getChild(tableName).hasStar())) {
                throw PrivilegeExceptions.alreadyHasPrivilege();
            } else if (!"*".equals(tableName) && (tableSucc || !dbNode.getChild(tableName).hasStar())) {
                dbNode.getChild(tableName).setStar();
            }
        } else if (!dbSuccess) {
            throw PrivilegeExceptions.alreadyHasPrivilege();
        }
    }

    /**
     * grant db.table and columns.
     *
     * @param dbName database
     * @param tableName table
     * @param colNames columns
     */
    public void grantPath(final String dbName, final String tableName, final List<String> colNames) {
        Boolean dbSuccess = root.addChild(dbName);
        if ("*".equals(dbName.trim()) && !dbSuccess) {
            throw PrivilegeExceptions.alreadyHasPrivilege();
        }
        if (!"*".equals(dbName.trim())) {
            Node dbNode = root.getChild(dbName);
            Boolean tableSuccess = dbNode.addChild(tableName);
            if ("*".equals(tableName.trim()) && !tableSuccess) {
                throw PrivilegeExceptions.alreadyHasPrivilege();
            }
            if (!"*".equals(tableName.trim())) {
                Node tableNode = dbNode.getChild(tableName);
                grantColNodes(tableNode, colNames);
            }
        }
    }

    /**
     * grant one column.
     *
     * @param dbName database
     * @param tableName table
     * @param colNames column
     */
    public void grantPath(final String dbName, final String tableName, final String colNames) {
        Boolean dbSuccess = root.addChild(dbName);
        if ("*".equals(dbName.trim()) && !dbSuccess) {
            throw PrivilegeExceptions.alreadyHasPrivilege();
        }
        if (!"*".equals(dbName.trim())) {
            Node dbNode = root.getChild(dbName);
            Boolean tableSuccess = dbNode.addChild(tableName);
            if ("*".equals(tableName.trim()) && !tableSuccess) {
                throw PrivilegeExceptions.alreadyHasPrivilege();
            }
            if (!"*".equals(tableName.trim())) {
                grantColNode(dbNode.getChild(tableName), colNames);
            }
        }
    }

    /**
     * revoke table.
     *
     * @param dbName database
     * @param tableName table
     */
    public void revokePath(final String dbName, final String tableName) {
        if ("*".equals(dbName.trim())) {
            if (!root.removeChild(dbName)) {
                throw PrivilegeExceptions.noSuchGrantDefined();
            } else {
                root.clearEmptyPaths();
                return;
            }
        }
        if (root.containsChild(dbName)) {
            Node dbNode = root.getChild(dbName);
            if (dbNode.containsChild(tableName) && dbNode.removeChild(tableName)) {
                dbNode.clearEmptyPaths();
                root.clearEmptyPaths();
            } else {
                throw PrivilegeExceptions.noSuchGrantDefined();
            }
        } else {
            throw PrivilegeExceptions.noSuchGrantDefined();
        }
    }

    /**
     * revoke columns.
     *
     * @param dbName database
     * @param tableName table
     * @param colNames columns
     */
    public void revokePath(final String dbName, final String tableName, final List<String> colNames) {
        if ("*".equals(dbName.trim())) {
            if (!root.removeChild(dbName)) {
                throw PrivilegeExceptions.noSuchGrantDefined();
            } else {
                root.clearEmptyPaths();
                return;
            }
        }
        if (root.containsChild(dbName)) {
            Node dbNode = root.getChild(dbName);
            if (dbNode.containsChild(tableName)) {
                Node tableNode = dbNode.getChild(tableName);
                removeColNodes(dbNode, tableNode, colNames);
            } else {
                throw PrivilegeExceptions.noSuchGrantDefined();
            }
        } else {
            throw PrivilegeExceptions.noSuchGrantDefined();
        }
    }

    /**
     * revoke column.
     *
     * @param dbName database
     * @param tableName table
     * @param colNames column
     */
    public void revokePath(final String dbName, final String tableName, final String colNames) {
        if (dbName.trim().equals("*")) {
            if (!root.removeChild(dbName)) {
                throw PrivilegeExceptions.noSuchGrantDefined();
            } else {
                root.clearEmptyPaths();
                return;
            }
        }
        if (root.containsChild(dbName)) {
            Node dbNode = root.getChild(dbName);
            if (dbNode.containsChild(tableName)) {
                Node tableNode = dbNode.getChild(tableName);
                removeColNode(dbNode, tableNode, colNames);
            } else {
                throw PrivilegeExceptions.noSuchGrantDefined();
            }
        } else {
            throw PrivilegeExceptions.noSuchGrantDefined();
        }
    }

    private void removeColNode(final Node dbNode,
                               final Node tableNode,
                               final String colNames) {
        if (!tableNode.removeChild(colNames)) {
            throw PrivilegeExceptions.noSuchGrantDefined();
        } else {
            if ("*".equals(colNames)) {
                tableNode.getOffspring().clear();
            }
            tableNode.clearEmptyPaths();
            dbNode.clearEmptyPaths();
            root.clearEmptyPaths();
        }
    }

    private void removeColNodes(final Node dbNode,
                                final Node tableNode,
                                final List<String> colNames) {
        Boolean removeSucc = false;
        Iterator<String> iterator = colNames.iterator();
        while (iterator.hasNext()) {
            String col = iterator.next();
            removeSucc = tableNode.removeChild(col) || removeSucc;
            if ("*".equals(col)) {
                tableNode.getOffspring().clear();
            }
        }
        if (!removeSucc) {
            throw PrivilegeExceptions.noSuchGrantDefined();
        } else {
            tableNode.clearEmptyPaths();
            dbNode.clearEmptyPaths();
            root.clearEmptyPaths();
        }
    }

    private void grantColNodes(final Node tableNode,
                               final List<String> colNames) {
        Boolean needUpdate = false;
        Iterator<String> iterator = colNames.iterator();
        while (iterator.hasNext()) {
            String curCol = iterator.next();
            needUpdate = tableNode.addChild(curCol) || needUpdate;
            if (!"*".equals(curCol)) {
                tableNode.getChild(curCol).setStar();
            }
        }
        if (!needUpdate) {
            throw PrivilegeExceptions.alreadyHasPrivilege();
        }
    }

    private void grantColNode(final Node tableNode,
                               final String colName) {
        Boolean needUpdate = false;
        needUpdate = tableNode.addChild(colName) || needUpdate;
        if (!"*".equals(colName)) {
            tableNode.getChild(colName).setStar();
        }
        if (!needUpdate) {
            throw PrivilegeExceptions.alreadyHasPrivilege();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tree tree = (Tree) o;
        return Objects.equals(root, tree.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
