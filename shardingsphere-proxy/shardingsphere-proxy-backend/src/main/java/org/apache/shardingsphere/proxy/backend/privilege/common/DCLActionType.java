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

public enum DCLActionType {

    CHECK, GRANT, REVOKE, DISABLE, CREATE, REMOVE, UNKNOWN_TYPE;

    /**
     * check which privilege action used.
     *
     * @param inputActionType input string
     * @return PrivilegeActionType.type
     */
    public static DCLActionType checkActionType(final String inputActionType) {
        switch (inputActionType.trim().toLowerCase()) {
            case "check":
                return CHECK;
            case "grant":
                return GRANT;
            case "revoke":
                return REVOKE;
            case "disable":
                return DISABLE;
            case "create":
                return CREATE;
            case "remove":
                return REMOVE;
            default:
                return UNKNOWN_TYPE;
        }
    }
}
