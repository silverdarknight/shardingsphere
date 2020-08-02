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

package org.apache.shardingsphere.proxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeModel;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public final class RolePrivilege extends PrivilegeModel implements Serializable {

    private static final long serialVersionUID = -1369825331445579126L;

    private String roleName;

    public RolePrivilege(final String roleName) {
        this.setRoleName(roleName);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        RolePrivilege that = (RolePrivilege) o;
        return Objects.equals(roleName, that.roleName)
                && super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roleName);
    }
}
