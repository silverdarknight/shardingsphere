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

package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.proxy.backend.privilege.model.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.model.UserPrivilege;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public interface AccessExecutorWrapper {

    /**
     * update user information.
     *
     * @param userInformationMap user information
     */
    void updateInformation(Map<String, UserInformation> userInformationMap);

    /**
     * update user privilege.
     *
     * @param userPrivilegeMap user privilege
     */
    void updateUsersPrivilege(Map<String, UserPrivilege> userPrivilegeMap);

    /**
     * update invalid user.
     *
     * @param invalidUserGroup invalid user group
     */
    void updateInvalidGroup(Collection<String> invalidUserGroup);

    /**
     * update role privilege.
     *
     * @param rolePrivilegeMap role privilege
     */
    void updateRolePrivileges(Map<String, RolePrivilege> rolePrivilegeMap);

    /**
     * access model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    byte[] toBytes() throws IOException;

    /**
     * information model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    byte[] informationToBytes() throws IOException;

    /**
     * invalidGroup model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    byte[] invalidGroupToBytes() throws IOException;

    /**
     * rolePrivileges model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    byte[] rolePrivilegesToBytes() throws IOException;

    /**
     * usersPrivilege model to byte.
     *
     * @return bytes
     * @throws IOException to byte error
     */
    byte[] usersPrivilegeToBytes() throws IOException;
}
