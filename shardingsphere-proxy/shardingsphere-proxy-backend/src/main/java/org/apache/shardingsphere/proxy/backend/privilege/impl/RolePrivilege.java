package org.apache.shardingsphere.proxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;

import java.io.Serializable;
import java.util.Objects;


public class RolePrivilege extends PrivilegeModel implements Serializable {

    @Getter
    @Setter
    private String roleName;

    public RolePrivilege(String roleName){
        this.setRoleName(roleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RolePrivilege that = (RolePrivilege) o;
        return Objects.equals(roleName, that.roleName) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roleName);
    }
}
