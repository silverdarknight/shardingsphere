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

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class PrivilegeActionTypeTest {

    @Test
    public void checkActionTypeTest() {
        assertThat(PrivilegeActionType.checkActionType("INSERT")
                == PrivilegeActionType.checkActionType("insert"), is(true));
        assertThat(PrivilegeActionType.checkActionType("insert"), is(PrivilegeActionType.INSERT));
        assertThat(PrivilegeActionType.checkActionType("delete"), is(PrivilegeActionType.DELETE));
        assertThat(PrivilegeActionType.checkActionType("select"), is(PrivilegeActionType.SELECT));
        assertThat(PrivilegeActionType.checkActionType("update"), is(PrivilegeActionType.UPDATE));
        assertThat(PrivilegeActionType.checkActionType("not_defined"), is(PrivilegeActionType.UNKNOWN_TYPE));
    }

    @Test
    public void canBuildModelTest() {
        assertThat(PrivilegeActionType.canGenerateModel(PrivilegeActionType.INSERT), is(true));
        assertThat(PrivilegeActionType.canGenerateModel(PrivilegeActionType.DELETE), is(true));
        assertThat(PrivilegeActionType.canGenerateModel(PrivilegeActionType.SELECT), is(true));
        assertThat(PrivilegeActionType.canGenerateModel(PrivilegeActionType.UPDATE), is(true));
        assertThat(PrivilegeActionType.canGenerateModel(PrivilegeActionType.UNKNOWN_TYPE), is(false));
    }
}
