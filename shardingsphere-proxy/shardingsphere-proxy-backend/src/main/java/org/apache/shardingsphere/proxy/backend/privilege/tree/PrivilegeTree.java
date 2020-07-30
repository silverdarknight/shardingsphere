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

package org.apache.shardingsphere.proxy.backend.privilege.tree;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeExceptions;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class PrivilegeTree implements PrivilegeTreeWrapper, Serializable {

    private static final long serialVersionUID = -9220671482019753933L;

    PrivilegeRootNode root = new PrivilegeRootNode("root");

    /**
     * check contains privilege
     *
     * @param dbName database name
     * @param tableName table name
     * @return have specific privilege
     */
    @Override
    public Boolean checkPath(String dbName, String tableName) {
        if(root.hasStar()) return true;
        if(root.containsChild(dbName)){
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            if(dbNode.hasStar()) return true;
            if(dbNode.containsChild(tableName)){
                PrivilegeTableNode tableNode = (PrivilegeTableNode) dbNode.getChild(tableName);
                if(tableNode.hasStar()) return true;
                else return false;
            }
            else return false;
        }
        else return false;
    }

    /**
     * check contains privilege
     *
     * @param dbName database name
     * @param tableName table name
     * @param colName column name
     * @return have specific privilege
     */
    @Override
    public Boolean checkPath(String dbName, String tableName, String colName) {
        if(root.hasStar()) return true;
        if(root.containsChild(dbName)){
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            if(dbNode.hasStar()) return true;
            if(dbNode.containsChild(tableName)){
                PrivilegeTableNode tableNode = (PrivilegeTableNode) dbNode.getChild(tableName);
                if(tableNode.hasStar() || tableNode.containsChild(colName)) return true;
                else return false;
            }
            else return false;
        }
        else return false;
    }

    /**
     * grant specific privilege
     *
     * @param dbName database name
     * @param tableName table name
     * @return whether grant specific privilege successfully
     */
    @Override
    public void grantPath(String dbName, String tableName) {
        Boolean dbSuccess = root.addChild(dbName);
        if(!dbName.trim().equals("*")) {
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            if (!dbNode.addChild(tableName))
                throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
            else if(!tableName.equals("*"))
                ((PrivilegeTableNode) dbNode.getChild(tableName)).setStar();
        }
        else if(!dbSuccess)
            throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
    }

    /**
     * grant specific privilege
     *
     * @param dbName database name
     * @param tableName table name
     * @param colNames columns
     * @return whether grant specific privilege successfully
     */
    @Override
    public void grantPath(String dbName, String tableName, List<String> colNames) {
        Boolean dbSuccess = root.addChild(dbName);
        if(dbName.trim().equals("*") && !dbSuccess)
            throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
        if(!dbName.trim().equals("*")){
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            Boolean tableSuccess = dbNode.addChild(tableName);
            if(tableName.trim().equals("*") && !tableSuccess)
                throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
            if(!tableName.trim().equals("*")){
                PrivilegeTableNode tableNode = (PrivilegeTableNode) dbNode.getChild(tableName);
                Boolean needUpdate = false;
                Iterator<String> iterator = colNames.iterator();
                while (iterator.hasNext()){
                    needUpdate = tableNode.addChild(iterator.next()) || needUpdate;
                }
                if(!needUpdate)
                    throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
            }
        }
    }

    @Override
    public void grantPath(String dbName, String tableName, String colNames) {
        Boolean dbSuccess = root.addChild(dbName);
        if(dbName.trim().equals("*") && !dbSuccess)
            throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
        if(!dbName.trim().equals("*")){
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            Boolean tableSuccess = dbNode.addChild(tableName);
            if(tableName.trim().equals("*") && !tableSuccess)
                throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
            if(!tableName.trim().equals("*")){
                PrivilegeTableNode tableNode = (PrivilegeTableNode) dbNode.getChild(tableName);
                if(!tableNode.addChild(colNames))
                    throw new ShardingSphereException(PrivilegeExceptions.alreadyHasPrivilege);
            }
        }
    }

    @Override
    public void revokePath(String dbName, String tableName) {
        if(dbName.trim().equals("*"))
            if(!root.removeChild(dbName))
                throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
            else {
                root.clearEmptyPaths();
                return;
            }
        if(root.containsNode(dbName)){
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            if(dbNode.containsNode(tableName)){
                // successfully remove table
                if(dbNode.removeChild(tableName)){
                    dbNode.clearEmptyPaths();
                    root.clearEmptyPaths();
                }
            }
            else throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
        }
        else throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
    }

    @Override
    public void revokePath(String dbName, String tableName, List<String> colNames) {
        if(dbName.trim().equals("*")) // *.*.*
            if(!root.removeChild(dbName))
                throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
            else{
                root.clearEmptyPaths();
                return;
            }
        if(root.containsNode(dbName)){
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            if(dbNode.containsNode(tableName)){
                PrivilegeTableNode tableNode = (PrivilegeTableNode) dbNode.getChild(tableName);
                Boolean removeSucc = false;
                Iterator<String> iterator = colNames.iterator();
                while (iterator.hasNext()){
                    String col = iterator.next();
                    removeSucc = tableNode.removeChild(col) || removeSucc;
                }
                if(!removeSucc)
                    throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
                else{
                    tableNode.clearEmptyPaths();
                    dbNode.clearEmptyPaths();
                    root.clearEmptyPaths();
                }
            }
            else throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
        }
        else throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
    }

    @Override
    public void revokePath(String dbName, String tableName, String colNames) {
        if(dbName.trim().equals("*")) // *.*.*
            if(!root.removeChild(dbName))
                throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
            else {
                root.clearEmptyPaths();
                return;
            }
        if(root.containsNode(dbName)){
            PrivilegeDBNode dbNode = (PrivilegeDBNode) root.getChild(dbName);
            if(dbNode.containsNode(tableName)){
                PrivilegeTableNode tableNode = (PrivilegeTableNode) dbNode.getChild(tableName);
                if(!tableNode.removeChild(colNames))
                    throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
                else {
                    tableNode.clearEmptyPaths();
                    dbNode.clearEmptyPaths();
                    root.clearEmptyPaths();
                }
            }
            else throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
        }
        else throw new ShardingSphereException(PrivilegeExceptions.noSuchGrantDefined);
    }
}
