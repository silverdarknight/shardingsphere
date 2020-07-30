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

package org.apache.shardingsphere.proxy.backend.privilege.tree;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeExceptions;

import java.io.Serializable;

public class PrivilegeColumnNode extends PrivilegeAbstractNode implements Serializable {

    private static final long serialVersionUID = 6547718177957008703L;

    public PrivilegeColumnNode(String content) {
        super(content);
    }

    @Override
    protected Boolean addChild(String path) {
        throw new ShardingSphereException(PrivilegeExceptions.cannotActNodeAfterColumn);
    }

    @Override
    protected Boolean removeChild(String path) {
        throw new ShardingSphereException(PrivilegeExceptions.cannotActNodeAfterColumn);
    }

    @Override
    protected Boolean containsChild(String path) {
        return false;
    }
}
