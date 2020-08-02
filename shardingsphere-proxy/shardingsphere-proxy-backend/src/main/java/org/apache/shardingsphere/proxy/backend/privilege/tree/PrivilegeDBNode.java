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

import java.io.Serializable;
import java.util.Iterator;

public class PrivilegeDBNode extends PrivilegeAbstractNode implements Serializable {

    private static final long serialVersionUID = -6266770940333025356L;

    public PrivilegeDBNode(final String content) {
        super(content);
    }

    @Override
    protected Boolean addChild(final String path) {
        if ("*".equals(path)) {
            if (getContainsStar()) {
                return false;
            }
            setContainsStar(true);
            return true;
        }
        PrivilegeTableNode tableNode = new PrivilegeTableNode(path);
        Iterator<PrivilegeAbstractNode> iterator = getOffspring().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getContent().equals(path)) {
                return false;
            }
        }
        getOffspring().add(tableNode);
        return true;
    }

    @Override
    protected Boolean removeChild(final String path) {
        if ("*".equals(path.trim())) {
            if (!getContainsStar()) {
                return false;
            }
            setContainsStar(false);
            return true;
        } else {
            Iterator<PrivilegeAbstractNode> iterator = getOffspring().iterator();
            while (iterator.hasNext()) {
                PrivilegeAbstractNode curTableNode = iterator.next();
                if (curTableNode.getContent().equals(path)) {
                    getOffspring().remove(curTableNode);
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected Boolean containsChild(final String path) {
        if (getContainsStar()) {
            return true;
        }
        Iterator<PrivilegeAbstractNode> iterator = getOffspring().iterator();
        while (iterator.hasNext()) {
            PrivilegeAbstractNode curTableNode = iterator.next();
            if (curTableNode.isPath(path)) {
                return true;
            }
        }
        return false;
    }
}
