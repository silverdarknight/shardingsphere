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

package org.apache.shardingsphere.proxy.backend.privilege.common;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;

public final class PrivilegeExceptions {

    /**
     * Do not have this permission.
     *
     * @return exception
     */
    public static ShardingSphereException notHaveCurrentPermission() {
        return new ShardingSphereException("You do not have this permission.");
    }

    /**
     * Can not add or remove child after column nodes.
     *
     * @return exception
     */
    public static ShardingSphereException cannotActNodeAfterColumn() {
        return new ShardingSphereException("Can not add or remove child after column nodes.");
    }

    /**
     * Already have this privileges.
     *
     * @return exception
     */
    public static ShardingSphereException alreadyHasPrivilege() {
        return new ShardingSphereException("Already have this privileges");
    }

    /**
     * There is no such grant defined.
     *
     * @return exception
     */
    public static ShardingSphereException noSuchGrantDefined() {
        return new ShardingSphereException("There is no such grant defined");
    }

    /**
     * Update model failed.
     *
     * @return exception
     */
    public static ShardingSphereException updateModelFailed() {
        return new ShardingSphereException("Update model failed.");
    }

    /**
     * No such user exception.
     *
     * @param userName user name
     * @return exception
     */
    public static ShardingSphereException noSuchUser(final String userName) {
        return new ShardingSphereException("No such user called " + userName);
    }

    /**
     * No such role exception.
     *
     * @param roleName user name
     * @return exception
     */
    public static ShardingSphereException noSuchRole(final String roleName) {
        return new ShardingSphereException("No such user called " + roleName);
    }

    /**
     * Already have a user exception.
     *
     * @param userName user name
     * @return exception
     */
    public static ShardingSphereException alreadyHaveUser(final String userName) {
        return new ShardingSphereException("Already have a user called " + userName);
    }

    /**
     * Already have a role exception.
     *
     * @param roleName user name
     * @return exception
     */
    public static ShardingSphereException alreadyHaveRole(final String roleName) {
        return new ShardingSphereException("Already have a user called " + roleName);
    }

    /**
     * DCL action type error.
     *
     * @return exception
     */
    public static ShardingSphereException actionTypeErrorException() {
        return new ShardingSphereException("DCL action type error.");
    }

    /**
     * Can not match privilege type.
     *
     * @return exception
     */
    public static ShardingSphereException noSuchPrivilegeType() {
        return new ShardingSphereException("Can not match privilege type");
    }

    public static ShardingSphereException UploadZookeeperError() {
        return new ShardingSphereException("Can not upload current model, please retry");
    }
}
