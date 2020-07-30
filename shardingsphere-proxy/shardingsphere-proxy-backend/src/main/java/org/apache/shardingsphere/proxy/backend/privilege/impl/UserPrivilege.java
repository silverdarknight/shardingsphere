package org.apache.shardingsphere.proxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.PrivilegeModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlUserPrivilegeConfiguration;

import java.io.Serializable;
import java.util.*;


@Getter
@Setter
public class UserPrivilege extends PrivilegeModel implements Serializable {

    private static final long serialVersionUID = -8606546448540297926L;
    
    private HashSet<String> roles = new HashSet<>();

    public List<String> getRolesName(){
        List<String> rolesName = new LinkedList<>();
        rolesName.addAll(getRoles());
        return rolesName;
    }

    public void grant(String role){
        this.getRoles().add(role);
    }

    public void revoke(String role){
        this.getRoles().remove(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserPrivilege that = (UserPrivilege) o;
        return Objects.equals(roles, that.roles) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roles);
    }
}
