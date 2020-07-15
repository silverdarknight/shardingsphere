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
        UserPrivilege userPrivilege = new UserPrivilege(new UserInformation("testUser","pw"));
        assertThat(userPrivilege,instanceOf(UserPrivilege.class));
    }

    @Test
    public void userPrivilegeCheckNotInRole(){
        // information (database / information)
        UserPrivilege userPrivilege = new UserPrivilege(new UserInformation("testUser","pw"));
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
        userPrivilege1 = new UserPrivilege(new UserInformation("testUser","pw"));
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        List<String> testCols = new LinkedList<>(); testCols.add("col1");testCols.add("col2");
        rolePrivilege.grant("select", "*.*"); // select all privilege
        rolePrivilege.grant("delete", "testDB_role.testTable_role",testCols); // delete testDB.testTable.col1;col2
        userPrivilege1.grant(rolePrivilege);
        assertThat(userPrivilege1.getRolesName().size(),is(1));
        userPrivilege1.grant(rolePrivilege);
        assertThat(userPrivilege1.getRolesName().size(),is(1));
        // information (database / information)
        userPrivilege1 = new UserPrivilege(new UserInformation("testUser","pw"));
        userPrivilege2 = new UserPrivilege(new UserInformation("testUser","pw"));
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
        userPrivilege1 = new UserPrivilege(new UserInformation("testUser","pw"));
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
        userPrivilege1.revoke(rolePrivilege);
        assertThat(userPrivilege1.getRolesName().size(),is(0));
        userPrivilege1.revoke(rolePrivilege);
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
    public void userPrivilegeCheckInRole(){
        UserPrivilege userPrivilege1;
        userPrivilege1 = new UserPrivilege(new UserInformation("testUser","pw"));
        userPrivilege1.grant("select", "testDB2.*");
        userPrivilege1.grant("select", "testDB.testTable2");
        List<String> testCols = new LinkedList<>(); testCols.add("col1");testCols.add("col2");
        userPrivilege1.grant("select", "testDB.testTable",testCols);
        RolePrivilege rolePrivilege = new RolePrivilege("testRole");
        rolePrivilege.grant("delete", "testDB_role.testTable_role",testCols); // delete testDB.testTable.col1;col2
        userPrivilege1.grant(rolePrivilege);
        // information (database / information)
        assertThat(userPrivilege1.checkPrivilege("select","testDB.testTable2"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB_role.testTable_role.col1"),is(true));
        // database table
        assertThat(userPrivilege1.checkPrivilege("select","testDB","testTable2"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB_role","testTable_role"),is(false));
        // database table column
        assertThat(userPrivilege1.checkPrivilege("select","testDB","testTable","col1"),is(true));
        assertThat(userPrivilege1.checkPrivilege("delete","testDB_role","testTable_role","col1"),is(true));
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
        userPrivilegeStdNoRole = new UserPrivilege(new UserInformation("testUser","pw"));
        userPrivilegeNoRole1 = new UserPrivilege(new UserInformation("testUser","pw"));
        userPrivilegeNoRole2 = new UserPrivilege(new UserInformation("testUser","pw"));
        userPrivilegeStdRole = new UserPrivilege(new UserInformation("testUser","pw"));
        userPrivilegeRole1 = new UserPrivilege(new UserInformation("testUser","pw"));
        userPrivilegeRole2 = new UserPrivilege(new UserInformation("testUser","pw"));
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
