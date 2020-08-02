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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class PrivilegeAbstractNode implements Serializable {

    private String content;

    private Collection<PrivilegeAbstractNode> offspring = new HashSet<>();

    private Boolean containsStar = false;

    private Boolean isRegNode;

    public PrivilegeAbstractNode(final String content) {
        isRegNode = contentIsReg(content);
        this.content = content.trim();
    }

    protected Boolean hasStar() {
        return containsStar;
    }

    protected void setStar() {
        containsStar = true;
    }

    protected String getContent() {
        return content;
    }

    /**
     * if node uses reg, check equals, or use likePath to check.
     *
     * @param path input node path
     */
    protected Boolean isPath(final String path) {
        if (!isRegNode) {
            return equalsContent(path);
        } else {
            return likePath(path);
        }
    }

    /**
     * check whether path.matches(reg).
     *
     * @param path input node path
     */
    protected Boolean likePath(final String path) {
        return true; }

    /**
     * input content, check whether type of content is reg.
     *
     * @param content input node path
     * @return is reg
     */
    protected Boolean contentIsReg(final String content) {
        return false;
    }

    /**
     * check empty node.
     *
     * @return offspring is empty and do not have *
     */
    protected Boolean containsOffspring() {
        return !offspring.isEmpty() || containsStar;
    }

    /**
     * foreach child node, if the node have zero child and do not have *, remove the child.
     *
     * @return whether this node need to be removed.
     */
    protected Boolean clearEmptyPaths() {
        if (containsOffspring()) {
            Iterator<PrivilegeAbstractNode> iterator = offspring.iterator();
            while (iterator.hasNext()) {
                PrivilegeAbstractNode nextGenNode = iterator.next();
                Boolean clearMySelf = nextGenNode.clearEmptyPaths();
                if (clearMySelf) {
                    iterator.remove();
                }
            }
            return !containsOffspring();
        } else {
            return true;
        }
    }

    /**
     * get specific node, return null if not exist.
     *
     * @param path child content
     * @return target node
     */
    protected PrivilegeAbstractNode getChild(final String path) {
        Iterator<PrivilegeAbstractNode> iterator = offspring.iterator();
        while (iterator.hasNext()) {
            PrivilegeAbstractNode curNode = iterator.next();
            if (curNode.content.equals(path.trim())) {
                return curNode;
            }
        }
        return null;
    }

    /**
     * contains node whose content.equals path.
     *
     * @param path input path
     * @return contains path
     */
    protected Boolean containsNode(final String path) {
        if (path.trim().equals("*") && containsStar) {
            return true;
        }
        Iterator<PrivilegeAbstractNode> iterator = offspring.iterator();
        while (iterator.hasNext()) {
            PrivilegeAbstractNode curTableNode = iterator.next();
            if (curTableNode.equalsContent(path)) {
                return true;
            }
        }
        return false;
    }

    private Boolean equalsContent(final String content) {
        return content.trim().equals(this.content);
    }

    protected abstract Boolean addChild(String path);

    protected abstract Boolean removeChild(String path);

    protected abstract Boolean containsChild(String path);

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivilegeAbstractNode that = (PrivilegeAbstractNode) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
