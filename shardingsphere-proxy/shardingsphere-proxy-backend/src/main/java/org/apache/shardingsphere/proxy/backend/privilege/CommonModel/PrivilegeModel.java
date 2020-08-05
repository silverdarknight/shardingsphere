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

package org.apache.shardingsphere.proxy.backend.privilege.CommonModel;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeActionType;
import org.apache.shardingsphere.proxy.backend.privilege.tree.PrivilegeTree;
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
public abstract class PrivilegeModel implements Serializable {

    private static final long serialVersionUID = -7687165906415647698L;

    private Map<PrivilegeActionType, PrivilegeTree> privilegePaths = new HashMap<>(PrivilegeActionType.values().length);

    public PrivilegeModel() {
        EnumSet<PrivilegeActionType> actionTypes = EnumSet.allOf(PrivilegeActionType.class);
        Iterator<PrivilegeActionType> iterator = actionTypes.iterator();
        while (iterator.hasNext()) {
            PrivilegeActionType curActionType = iterator.next();
            if (PrivilegeActionType.canGenerateModel(curActionType)) {
                getPrivilegePaths().put(curActionType, new PrivilegeTree());
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
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("insert",
                        yamlPrivilegePath.getInformation());
            }
        }
        // delete
        iterator = yamlPrivilegeConfiguration.getDelete().iterator();
        while (iterator.hasNext()) {
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if (yamlPrivilegePath.getCols() != null && yamlPrivilegePath.getCols().size() != 0) {
                this.grant("delete",
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("delete",
                        yamlPrivilegePath.getInformation());
            }
        }
        // select
        iterator = yamlPrivilegeConfiguration.getSelect().iterator();
        while (iterator.hasNext()) {
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if (yamlPrivilegePath.getCols() != null && yamlPrivilegePath.getCols().size() != 0) {
                this.grant("select",
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("select",
                        yamlPrivilegePath.getInformation());
            }
        }
        // update
        iterator = yamlPrivilegeConfiguration.getUpdate().iterator();
        while (iterator.hasNext()) {
            YamlPrivilegePath yamlPrivilegePath = iterator.next();
            if (yamlPrivilegePath.getCols() != null && yamlPrivilegePath.getCols().size() != 0) {
                this.grant("update",
                        yamlPrivilegePath.getInformation(),
                        yamlPrivilegePath.getCols());
            } else {
                this.grant("update",
                        yamlPrivilegePath.getInformation());
            }
        }
    }

    protected PrivilegeTree chosePrivilegeType(final String privilegeType) {
        PrivilegeActionType actionType = PrivilegeActionType.checkActionType(privilegeType);
        if (actionType == PrivilegeActionType.UNKNOWN_TYPE) {
            throw new ShardingSphereException("Can not match privilege type");
        } else {
            return getPrivilegePaths().get(actionType);
        }
    }

    public void grant(final String privilegeType,
                         final String dbName,
                         final String tableName,
                         final List<String> cols) {
        PrivilegeTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.grantPath(dbName, tableName, cols);
    }

    public void grant(final String privilegeType, final String dbName, final String tableName) {
        PrivilegeTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.grantPath(dbName, tableName);
    }

    public void grant(final String privilegeType,
                         final String information) {
        String[] splitInfo = splitInformation(information);
        if (splitInfo.length == 1) {
            throw new ShardingSphereException("illegal input target database and table");
        } else if (splitInfo.length == 2) {
            grant(privilegeType, splitInfo[0], splitInfo[1]);
        } else {
            throw new ShardingSphereException("illegal input target database and table");
        }
    }

    public void grant(final String privilegeType,
                         final String information,
                         final List<String> cols) {
        String[] splitInfo = splitInformation(information);
        if (splitInfo.length == 1) {
            throw new ShardingSphereException("illegal input target database and table");
        } else if (splitInfo.length == 2) {
            grant(privilegeType, splitInfo[0], splitInfo[1], cols);
        } else {
            throw new ShardingSphereException("illegal input target database and table");
        }
    }

    public void revoke(final String privilegeType,
                          final String dbName,
                          final String tableName,
                          final List<String> cols) {
        PrivilegeTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.revokePath(dbName, tableName, cols);
    }

    public void revoke(final String privilegeType, final String dbName, final String tableName) {
        PrivilegeTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        targetPrivilegeTree.revokePath(dbName, tableName);
    }

    public void revoke(final String privilegeType, final String information) {
        String[] splitInfo = splitInformation(information);
        if (splitInfo.length == 1) {
            revoke(privilegeType, splitInfo[0]);
        } else if (splitInfo.length == 2) {
            revoke(privilegeType, splitInfo[0], splitInfo[1]);
        } else {
            throw new ShardingSphereException("illegal input target database and table");
        }
    }

    public void revoke(final String privilegeType,
                          final String information,
                          final List<String> cols) {
        String[] splitInfo = splitInformation(information);
        if (splitInfo.length == 1) {
            throw new ShardingSphereException("illegal input target database and table");
        } else if (splitInfo.length == 2) {
            revoke(privilegeType, splitInfo[0], splitInfo[1], cols);
        } else {
            throw new ShardingSphereException("illegal input target database and table");
        }
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
        PrivilegeTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
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
        PrivilegeTree targetPrivilegeTree = chosePrivilegeType(privilegeType);
        return targetPrivilegeTree.checkPath(dbName, tableName);
    }

    /**
     * check privilege.
     *
     * @param privilegeType type
     * @param information db and table
     * @return have privilege
     */
    public boolean checkPrivilege(final String privilegeType, final String information) {
        String[] splitInfo = splitInformation(information);
        if (splitInfo.length == 1) {
            throw new ShardingSphereException("illegal input target database and table");
        } else if (splitInfo.length == 2) {
            return checkPrivilege(privilegeType, splitInfo[0], splitInfo[1]);
        } else {
            return checkPrivilege(privilegeType, splitInfo[0], splitInfo[1], splitInfo[2]);
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
        PrivilegeModel that = (PrivilegeModel) o;
        return Objects.equals(privilegePaths, that.privilegePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privilegePaths);
    }

    private String[] splitInformation(final String information) {
        String[] dbAndTable = information.split("\\.");
        return dbAndTable;
    }
}
