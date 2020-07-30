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

package org.apache.shardingsphere.proxy.backend.privilege;

import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class UserPrivilegeTest {

    @Test
    public void userPrivilegeConstruct(){
        UserPrivilege userPrivilege = new UserPrivilege();
        assertThat(userPrivilege,instanceOf(UserPrivilege.class));
    }

    @Test
    public void userPrivilegeCheckNotInRole(){
        // information (database / information)
        UserPrivilege userPrivilege = new UserPrivilege();
        List<String> testCols = new LinkedList<>(); testCols.add("col1");testCols.add("col2");
        userPrivilege.grant("select", "*.*"); // select all privilege
        userPrivilege.grant("delete", "testDB.testTable",testCols); // delete testDB.testTable.col1;col2
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable"),is(false));
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable.col1"),is(true));
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("delete","testDB.testTable.col_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("select","testDB_false.testTable_false"),is(true));
        // database table
        assertThat(userPrivilege.checkPrivilege("delete","testDB", "testTable"),is(false));
        assertThat(userPrivilege.checkPrivilege("delete","testDB", "testTable_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("select","testDB_false", "testTable_false"),is(true));
        // database table column
        assertThat(userPrivilege.checkPrivilege("delete","testDB", "testTable", "col1"),is(true));
        assertThat(userPrivilege.checkPrivilege("delete","testDB_false", "testTable", "col_false"),is(false));
        assertThat(userPrivilege.checkPrivilege("select","testDB_false", "testTable_false", "col_false"),is(true));
    }

    @Test
    public void userPrivilegeGrant(){
        UserPrivilege userPrivilege1,userPrivilege2;
        // role
        userPrivilege1 = new UserPrivilege();
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        List<String> testCols = new LinkedList<>(); testCols.add("col1");testCols.add("col2");
        rolePrivilege.grant("select", "*.*"); // select all privilege
        rolePrivilege.grant("delete", "testDB_role.testTable_role",testCols); // delete testDB.testTable.col1;col2
        userPrivilege1.grant(rolePrivilege.getRoleName());
        assertThat(userPrivilege1.getRolesName().size(),is(1));
        userPrivilege1.grant(rolePrivilege.getRoleName());
        assertThat(userPrivilege1.getRolesName().size(),is(1));
        // information (database / information)
        userPrivilege1 = new UserPrivilege();
        userPrivilege2 = new UserPrivilege();
        userPrivilege1.grant("select", "*.*");
        userPrivilege2.grant("select", "testDB2.testTable");
        assertThat(userPrivilege2.checkPrivilege("select","testDB2.testTable"),is(true));
        assertThat(userPrivilege2.checkPrivilege("select","testDB2.testTable_false"),is(false));
        // database table
        userPrivilege1.grant("delete","testDB","testTable");
        assertThat(userPrivilege1.checkPrivilege("delete","testDB","testTable"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB","testTable_false"),is(false));
        // database table column
        List<String> cols = new LinkedList<>(); cols.add("col1");
        userPrivilege1.grant("update","testDB","testTable",cols);
        assertThat(userPrivilege1.checkPrivilege("update","testDB","testTable","col1"),is(true));
        assertThat(userPrivilege1.checkPrivilege("update","testDB","testTable","col_false"),is(false));
    }

    @Test
    public void userPrivilegeRevoke(){
        UserPrivilege userPrivilege1,userPrivilege2;
        userPrivilege1 = new UserPrivilege();
        userPrivilege1.grant("select", "testDB2.*");
        List<String> testCols = new LinkedList<>(); testCols.add("col1");
        userPrivilege1.grant("select", "testDB.testTable",testCols);
        userPrivilege1.grant("select", "testDB.testTable2");
        testCols = new LinkedList<>(); testCols.add("col3");
        userPrivilege1.grant("select", "testDB.testTable",testCols);
        //role
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        rolePrivilege.grant("select", "*.*"); // select all privilege
        testCols = new LinkedList<>(); testCols.add("col1");testCols.add("col2");
        rolePrivilege.grant("delete", "testDB_role.testTable_role",testCols); // delete testDB.testTable.col1;col2
        userPrivilege1.revoke(rolePrivilege.getRoleName());
        assertThat(userPrivilege1.getRolesName().size(),is(0));
        userPrivilege1.revoke(rolePrivilege.getRoleName());
        assertThat(userPrivilege1.getRolesName().size(),is(0));
        // information (database / information)
        List<String> cols = new LinkedList<>(); cols.add("col1");
        userPrivilege1.revoke("select","testDB.testTable",cols);
        userPrivilege1.revoke("select","testDB2.*");
        assertThat(userPrivilege1.checkPrivilege("select","testDB2.error"),is(false));
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable.col1"),is(false));
        // database table
        userPrivilege1.revoke("select","testDB", "testTable2");
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable2"),is(false));
        // database table column
        cols = new LinkedList<>(); cols.add("col3");
        userPrivilege1.revoke("select","testDB", "testTable",cols);
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable.col3"),is(false));
    }

    @Test
    public void userPrivilegeEquals(){
        UserPrivilege userPrivilegeStdNoRole,userPrivilegeNoRole1,userPrivilegeNoRole2
                ,userPrivilegeStdRole,userPrivilegeRole1,userPrivilegeRole2;
        PrivilegePathTreeNode tree1 = new PrivilegePathTreeNode()
                , child1_1 = new PrivilegePathTreeNode("1",tree1)
                , child1_1_1 = new PrivilegePathTreeNode("1",child1_1)
                , tree2 = new PrivilegePathTreeNode()
                , child2_1 = new PrivilegePathTreeNode("1",tree2)
                , child2_1_1 = new PrivilegePathTreeNode("1",child2_1);
        assertThat(child1_1.equals(child2_1),is(true));
        tree1.getOffspring().add(child1_1);tree2.getOffspring().add(child2_1);
        assertThat(tree1.equals(tree2),is(true));assertThat(tree1.hashCode()==tree2.hashCode(),is(true));
        child1_1.getOffspring().add(child1_1_1);child2_1.getOffspring().add(child2_1_1);
        assertThat(child1_1.equals(child2_1),is(true));
        assertThat(child1_1.getOffspring().contains(child2_1_1),is(true));
        assertThat(tree1.equals(tree2),is(true));
        userPrivilegeStdNoRole = new UserPrivilege();
        userPrivilegeNoRole1 = new UserPrivilege();
        userPrivilegeNoRole2 = new UserPrivilege();
        userPrivilegeStdRole = new UserPrivilege();
        userPrivilegeRole1 = new UserPrivilege();
        userPrivilegeRole2 = new UserPrivilege();
        userPrivilegeStdNoRole.grant("select", "testDB2.*");
        userPrivilegeNoRole1.grant("select", "testDB2 .*");
        userPrivilegeNoRole2.grant("select", "testDB3.*");
        // without role
        assertThat(userPrivilegeStdNoRole.equals(userPrivilegeNoRole1),is(true));
        assertThat(userPrivilegeStdNoRole.equals(userPrivilegeNoRole2),is(false));
        assertThat(userPrivilegeStdNoRole.hashCode()==userPrivilegeNoRole1.hashCode(),is(true));
        assertThat(userPrivilegeStdNoRole.hashCode()==userPrivilegeNoRole2.hashCode(),is(false));
        // role
        RolePrivilege rolePrivilege = new RolePrivilege("testRole")
                , rolePrivilege1 = new RolePrivilege("testRole")
                , rolePrivilege2 = new RolePrivilege("testRole");
        List<String> testCols = new LinkedList<>(); testCols.add("col1");testCols.add("col2");
        rolePrivilege.grant("select", "testDB_role.testTable_role",testCols);
        testCols = new LinkedList<>(); testCols.add("col1 ");testCols.add("col2");
        rolePrivilege1.grant("select", "testDB_role.testTable_role",testCols);
        testCols = new LinkedList<>(); testCols.add("col1");testCols.add("col3");
        rolePrivilege2.grant("select", "testDB_role.testTable_role",testCols);
        assertThat(rolePrivilege.equals(rolePrivilege1),is(true));
        assertThat(rolePrivilege.equals(rolePrivilege2),is(false));
        assertThat(rolePrivilege.hashCode()==rolePrivilege1.hashCode(),is(true));
        assertThat(rolePrivilege.hashCode()==rolePrivilege2.hashCode(),is(false));
    }
}
