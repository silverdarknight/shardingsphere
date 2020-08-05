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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Objects;

@Getter
@Setter
public final class UserPrivilege extends PrivilegeModel implements Serializable {

    private static final long serialVersionUID = -8606546448540297926L;
    
    private Collection<String> roles = new HashSet<>();

    /**
     * get role name list.
     *
     * @return role name list
     */
    public List<String> getRolesName() {
        List<String> rolesName = new LinkedList<>();
        rolesName.addAll(getRoles());
        return rolesName;
    }

    /**
     * grant role for user.
     *
     * @param role role name
     */
    public void grant(final String role) {
        this.getRoles().add(role);
    }

    /**
     * revoke role for user.
     *
     * @param role role name
     */
    public void revoke(final String role) {
        this.getRoles().remove(role);
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
        UserPrivilege that = (UserPrivilege) o;
        return Objects.equals(roles, that.roles)
                && super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roles);
    }
}
