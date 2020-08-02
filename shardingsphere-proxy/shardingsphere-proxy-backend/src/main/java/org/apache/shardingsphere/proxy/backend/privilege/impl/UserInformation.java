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

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public final class UserInformation implements Serializable {

    private static final long serialVersionUID = 2847558164326116634L;

    private static String rootUser = "ROOT";

    private static String defaultUser = "ANONYMOUS";

    private String userName;

    private String password;

    public UserInformation(final String userName, final String password) {
        this.setUserName(userName);
        this.setPassword(password);
    }


    /**
     * get root user.
     *
     * @return root user name
     */
    public static String getRootUser() {
        return rootUser;
    }

    /**
     * get default user.
     *
     * @return default user name
     */
    public static String getDefaultUser() {
        return defaultUser;
    }

    /**
     * set user password.
     *
     * @param password password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserInformation that = (UserInformation) o;
        return Objects.equals(userName, that.userName)
                && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password);
    }
}
