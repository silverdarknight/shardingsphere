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

package org.apache.shardingsphere.proxy.backend.privilege.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeActionType;
import org.apache.shardingsphere.proxy.backend.privilege.tree.Tree;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegePath;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class PrivilegeModel implements Serializable {

    private static final long serialVersionUID = -7687165906415647698L;

    private Map<PrivilegeActionType, Tree> privilegePaths = new HashMap<>(PrivilegeActionType.values().length);

    public PrivilegeModel() {
        EnumSet<PrivilegeActionType> actionTypes = EnumSet.allOf(PrivilegeActionType.class);
        Iterator<PrivilegeActionType> iterator = actionTypes.iterator();
        while (iterator.hasNext()) {
            PrivilegeActionType curActionType = iterator.next();
            if (PrivilegeActionType.canGenerateModel(curActionType)) {
                getPrivilegePaths().put(curActionType, new Tree());
            }
        }
    }

    /**
     * construct model with yaml config model.
     *
     * @param yamlPrivilegeConfiguration yaml model
     */
    public void constructModel(final YamlPrivilegeConfiguration yamlPrivilegeConfiguration) {
        // insert
        Iterator<YamlPrivilegePath> iterator = yamlPrivilegeConfiguration.getInsert().iterator();
        while (iterator.hasNext()) {
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if (yamlPrivilegePath.getCols() != null && yamlPrivilegePath.getCols().size() != 0) {
                this.grant("insert",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1],
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("insert",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1]);
            }
        }
        // delete
        iterator = yamlPrivilegeConfiguration.getDelete().iterator();
        while (iterator.hasNext()) {
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if (yamlPrivilegePath.getCols() != null && yamlPrivilegePath.getCols().size() != 0) {
                this.grant("delete",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1],
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("delete",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1]);
            }
        }
        // select
        iterator = yamlPrivilegeConfiguration.getSelect().iterator();
        while (iterator.hasNext()) {
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if (yamlPrivilegePath.getCols() != null && yamlPrivilegePath.getCols().size() != 0) {
                this.grant("select",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1],
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("select",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1]);
            }
        }
        // update
        iterator = yamlPrivilegeConfiguration.getUpdate().iterator();
        while (iterator.hasNext()) {
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if (yamlPrivilegePath.getCols() != null && yamlPrivilegePath.getCols().size() != 0) {
                this.grant("update",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1],
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("update",
                        yamlPrivilegePath.getInformation().split("\\.")[0],
                        yamlPrivilegePath.getInformation().split("\\.")[1]);
            }
        }
    }

    protected Tree chosePrivilegeType(final String privilegeType) {
        PrivilegeActionType actionType = PrivilegeActionType.checkActionType(privilegeType);
        if (actionType == PrivilegeActionType.UNKNOWN_TYPE) {
            throw new ShardingSphereException("Can not match privilege type");
        } else {
            return getPrivilegePaths().get(actionType);
        }
    }

    /**
     * grant columns privilege.
     *
     * @param privilegeType privilege type
     * @param dbName db name
     * @param tableName table name
     * @param cols columns
     */
    public void grant(final String privilegeType,
                         final String dbName,
                         final String tableName,
                         final List<String> cols) {
        Tree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.grantPath(dbName, tableName, cols);
    }

    /**
     * grant table privileges.
     *
     * @param privilegeType privilege type
     * @param dbName db name
     * @param tableName table name
     */
    public void grant(final String privilegeType, final String dbName, final String tableName) {
        Tree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.grantPath(dbName, tableName);
    }

    /**
     * revoke columns privilege.
     *
     * @param privilegeType privilege type
     * @param dbName db name
     * @param tableName table name
     * @param cols columns
     */
    public void revoke(final String privilegeType,
                          final String dbName,
                          final String tableName,
                          final List<String> cols) {
        Tree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.revokePath(dbName, tableName, cols);
    }

    /**
     * revoke table privilege.
     *
     * @param privilegeType privilege type
     * @param dbName db name
     * @param tableName table name
     */
    public void revoke(final String privilegeType, final String dbName, final String tableName) {
        Tree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.revokePath(dbName, tableName);
    }

    /**
     * check privilege.
     *
     * @param privilegeType type
     * @param dbName database
     * @param tableName table
     * @param column column
     * @return have privilege
     */
    public boolean checkPrivilege(final String privilegeType,
                                  final String dbName,
                                  final String tableName,
                                  final String column) {
        Tree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        return targetPrivilegeTree.checkPath(dbName, tableName, column);
    }

    /**
     * check privilege.
     *
     * @param privilegeType type
     * @param dbName database
     * @param tableName table
     * @return have privilege
     */
    public boolean checkPrivilege(final String privilegeType, final String dbName, final String tableName) {
        Tree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        return targetPrivilegeTree.checkPath(dbName, tableName);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivilegeModel that = (PrivilegeModel) o;
        return Objects.equals(privilegePaths, that.privilegePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privilegePaths);
    }

    /**
     * split tradition db.table.
     *
     * @param information db.table
     * @return new int[]{db, table}
     */
    public static String[] splitInformation(final String information) {
        String[] dbAndTable = information.split("\\.");
        return dbAndTable;
    }
}
