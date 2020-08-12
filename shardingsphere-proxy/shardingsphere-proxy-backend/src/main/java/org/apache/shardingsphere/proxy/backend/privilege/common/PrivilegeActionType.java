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

public enum PrivilegeActionType {

    INSERT, DELETE, SELECT, UPDATE, UNKNOWN_TYPE;

    /**
     * check which privilege action used.
     *
     * @param inputActionType input string
     * @return PrivilegeActionType.type
     */
    public static PrivilegeActionType checkActionType(final String inputActionType) {
        switch (inputActionType.trim().toLowerCase()) {
            case "insert":
                return INSERT;
            case "delete":
                return DELETE;
            case "select":
                return SELECT;
            case "update":
                return UPDATE;
            default:
                return UNKNOWN_TYPE;
        }
    }

    /**
     * can type generate model in access model.
     *
     * @param type action type
     * @return can generate
     */
    public static Boolean canGenerateModel(final PrivilegeActionType type) {
        if (PrivilegeActionType.INSERT == type) {
            return true;
        } else if (PrivilegeActionType.DELETE == type) {
            return true;
        } else if (PrivilegeActionType.SELECT == type) {
            return true;
        } else {
            return PrivilegeActionType.UPDATE == type;
        }
    }
}
