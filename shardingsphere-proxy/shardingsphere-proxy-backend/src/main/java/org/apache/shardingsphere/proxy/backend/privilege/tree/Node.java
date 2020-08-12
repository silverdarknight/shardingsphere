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

import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Objects;

@Getter(AccessLevel.PUBLIC)
public class Node implements Serializable {

    private static final long serialVersionUID = 8576835394234353733L;

    private final String content;

    private final List<Node> offspring = new LinkedList<>();

    private Boolean containsStar = false;

    public Node(final String path) {
        content = path;
    }

    /**
     * whether node has *.
     *
     * @return has *
     */
    public Boolean hasStar() {
        return containsStar;
    }

    /**
     * set * for node.
     *
     */
    public void setStar() {
        containsStar = true;
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
            Iterator<Node> iterator = offspring.iterator();
            while (iterator.hasNext()) {
                Node nextGenNode = iterator.next();
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
    public Node getChild(final String path) {
        Iterator<Node> iterator = offspring.iterator();
        while (iterator.hasNext()) {
            Node curNode = iterator.next();
            if (curNode.getContent().equals(path)) {
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
    public Boolean containsChild(final String path) {
        if ("*".equals(path) && containsStar) {
            return true;
        }
        Iterator<Node> iterator = offspring.iterator();
        while (iterator.hasNext()) {
            Node curTableNode = iterator.next();
            if (curTableNode.equalsContent(path)) {
                return true;
            }
        }
        return false;
    }

    private Boolean equalsContent(final String content) {
        return content.equals(this.content);
    }

    /**
     * add child node.
     *
     * @param path content path
     * @return add successful
     */
    public Boolean addChild(final String path) {
        if ("*".equals(path)) {
            if (getContainsStar()) {
                return false;
            }
            containsStar = true;
            return true;
        }
        Node colNode = new Node(path);
        Iterator<Node> iterator = getOffspring().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getContent().equals(path)) {
                return false;
            }
        }
        getOffspring().add(colNode);
        return true;
    }

    /**
     * remove child.
     *
     * @param path content path
     * @return remove success
     */
    public Boolean removeChild(final String path) {
        if ("*".equals(path)) {
            if (!containsOffspring()) {
                return false;
            }
            containsStar = false;
            return true;
        } else {
            Iterator<Node> iterator = getOffspring().iterator();
            while (iterator.hasNext()) {
                Node curColNode = iterator.next();
                if (curColNode.getContent().equals(path)) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node node = (Node) o;
        return Objects.equals(content, node.content)
                && Objects.equals(offspring, node.offspring)
                && Objects.equals(containsStar, node.containsStar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, offspring, containsStar);
    }
}
