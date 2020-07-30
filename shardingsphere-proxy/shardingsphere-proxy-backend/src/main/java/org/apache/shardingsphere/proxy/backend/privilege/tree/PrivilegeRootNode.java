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

public class PrivilegeRootNode extends PrivilegeAbstractNode implements Serializable {

    private static final long serialVersionUID = 7141817834982157484L;

    public PrivilegeRootNode(String content) {
        super(content);
    }

    @Override
    protected Boolean addChild(String path) {
        if(path.equals("*")) {
            if(containsStar) return false;
            containsStar = true;
            return true;
        }
        PrivilegeDBNode dbNode = new PrivilegeDBNode(path);
        Iterator<PrivilegeAbstractNode> iterator = offspring.iterator();
        while (iterator.hasNext()){
            if(iterator.next().getContent().equals(path)) return false;
        }
        offspring.add(dbNode);
        return true;
    }

    @Override
    protected Boolean removeChild(String path) {
        if(path.trim().equals("*")) {
            if(!containsStar) return false;
            containsStar = false;
            return true;
        }
        else{
            Iterator<PrivilegeAbstractNode> iterator = offspring.iterator();
            while (iterator.hasNext()){
                PrivilegeAbstractNode curDBNode = iterator.next();
                if(curDBNode.getContent().equals(path)) {
                    offspring.remove(curDBNode);
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected Boolean containsChild(String path) {
        if(containsStar) return true;
        Iterator<PrivilegeAbstractNode> iterator = offspring.iterator();
        while (iterator.hasNext()){
            PrivilegeAbstractNode curDBNode = iterator.next();
            if(curDBNode.isPath(path)) return true;
        }
        return false;
    }
}
