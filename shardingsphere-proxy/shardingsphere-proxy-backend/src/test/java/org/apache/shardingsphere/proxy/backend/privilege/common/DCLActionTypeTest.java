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

public class DCLActionTypeTest {

    @Test
    public void checkTest() {
        assertThat(DCLActionType.checkActionType("INSERT"), is(DCLActionType.checkActionType("insert")));
        assertThat(DCLActionType.checkActionType("check"), is(DCLActionType.CHECK));
        assertThat(DCLActionType.checkActionType("GRANT"), is(DCLActionType.GRANT));
        assertThat(DCLActionType.checkActionType("REVOKE"), is(DCLActionType.REVOKE));
        assertThat(DCLActionType.checkActionType("DISABLE"), is(DCLActionType.DISABLE));
        assertThat(DCLActionType.checkActionType("CREATE"), is(DCLActionType.CREATE));
        assertThat(DCLActionType.checkActionType("REMOVE"), is(DCLActionType.REMOVE));
        assertThat(DCLActionType.checkActionType("UNKNOWN_TYPE_undefined"), is(DCLActionType.UNKNOWN_TYPE));
    }
}
