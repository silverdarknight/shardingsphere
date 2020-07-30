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
import org.apache.shardingsphere.proxy.backend.privilege.tree.PrivilegeTree;
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
}
