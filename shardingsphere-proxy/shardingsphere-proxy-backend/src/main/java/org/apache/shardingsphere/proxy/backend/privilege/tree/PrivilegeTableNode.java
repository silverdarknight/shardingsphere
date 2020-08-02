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

public class PrivilegeTableNode extends PrivilegeAbstractNode implements Serializable {

    private static final long serialVersionUID = -2332527780449248548L;

    public PrivilegeTableNode(final String content) {
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
        PrivilegeColumnNode colNode = new PrivilegeColumnNode(path);
        Iterator<PrivilegeAbstractNode> iterator = getOffspring().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getContent().equals(path)) {
                return false;
            }
        }
        getOffspring().add(colNode);
        return true;
    }

    @Override
    protected Boolean removeChild(final String path) {
        if ("*".equals(path.trim())) {
            if (!containsOffspring()) {
                return false;
            }
            setContainsStar(false);
            getOffspring().clear();
            return true;
        } else {
            Iterator<PrivilegeAbstractNode> iterator = getOffspring().iterator();
            while (iterator.hasNext()) {
                PrivilegeAbstractNode curColNode = iterator.next();
                if (curColNode.getContent().equals(path)) {
                    getOffspring().remove(curColNode);
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
            PrivilegeAbstractNode curColNode = iterator.next();
            if (curColNode.isPath(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Boolean clearEmptyPaths() {
        return !containsOffspring();
    }
}
