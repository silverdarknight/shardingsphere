package org.apache.shardingsphere.proxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class UserInformation {

    public static String ROOT_USER = "ROOT";

    private String userName;

    private String password;

    public UserInformation(String userName, String password){
        this.setUserName(userName);
        this.setPassword(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // private Object etc;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInformation that = (UserInformation) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password);
    }
}
