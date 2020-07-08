package org.apache.shardingsphere.proxy.backend.privilege;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class PrivilegePathTest {
    @Test
    public void assertPrivilegeConstructor(){
        // information (database / information)
        PrivilegePath privilegePath = new PrivilegePath();
        assertThat(privilegePath.getPrivilegeInformation(),is("*.*.*"));
        PrivilegePath privilegePath1 = new PrivilegePath("testDB");
        assertThat(privilegePath1.getPrivilegeInformation(),is("testDB.*.*"));
        PrivilegePath privilegePath2 = new PrivilegePath("testDB.testTable");
        assertThat(privilegePath2.getPrivilegeInformation(),is("testDB.testTable.*"));
        PrivilegePath privilegePath3 = new PrivilegePath("testDB.testTable.col1;col2");
        assertThat(privilegePath3.getPrivilegeInformation(),is("testDB.testTable.col1;col2"));
        PrivilegePath privilegePath4 = new PrivilegePath("testDB");
        assertThat(privilegePath4.getPrivilegeInformation(),is("testDB.*.*"));
        // database table
        PrivilegePath privilegePath5 = new PrivilegePath("testDB","testTable");
        assertThat(privilegePath5.getPrivilegeInformation(),is("testDB.testTable.*"));
        // database table cols
        List<String> cols = new LinkedList<>();cols.add("col1");cols.add("col2");
        PrivilegePath privilegePath6 = new PrivilegePath("testDB","testTable",cols);
        assertThat(privilegePath6.getPrivilegeInformation(),is("testDB.testTable.col1;col2"));
    }

    @Test
    public void assertPrivilegeEquals(){
        PrivilegePath privilegePath1 = new PrivilegePath("testDB.testTable")
                , privilegePath2 = new PrivilegePath("testDB.testTable")
                , privilegePath3 = new PrivilegePath("testDB.testTable_diff")
                , privilegePath4 = new PrivilegePath("testDB .testTable")
                , privilegePath5 = new PrivilegePath("testDB. testTable");
        assertThat(privilegePath1.equals(privilegePath2),is(true));
        assertThat(privilegePath1.equals(privilegePath3),is(false));
        assertThat(privilegePath1.equals(privilegePath4),is(true));
        assertThat(privilegePath1.equals(privilegePath5),is(true));
        assertThat(privilegePath4.equals(privilegePath5),is(true));
        assertThat(privilegePath1.hashCode() == privilegePath2.hashCode(),is(true));
        assertThat(privilegePath1.hashCode() == privilegePath3.hashCode(),is(false));
        assertThat(privilegePath1.hashCode() == privilegePath4.hashCode(),is(true));
        assertThat(privilegePath1.hashCode() == privilegePath5.hashCode(),is(true));
        assertThat(privilegePath4.hashCode() == privilegePath5.hashCode(),is(true));
    }

    @Test
    public void assertContainPlace(){
        PrivilegePath privilegePath1, privilegePath2, privilegePath3, privilegePath4;
        // information (database)
        privilegePath1 = new PrivilegePath();
        privilegePath2 = new PrivilegePath("testDB");
        privilegePath3 = new PrivilegePath("testDB.testTable");
        privilegePath4 = new PrivilegePath("testDB.testTable.col1;col2");
        assertThat(privilegePath1.containsTargetPlace("testDB"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB_false"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false"),is(false));
        // table
        assertThat(privilegePath3.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilegePath3.containsTargetPlace("testDB","testTable_false"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilegePath3.containsTargetPlace("testDB.testTable_false"),is(false));

        assertThat(privilegePath1.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB","testTable_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false","testTable"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false","testTable_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB.testTable_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false.testTable"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false.testTable_false"),is(true));

        assertThat(privilegePath2.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB","testTable_false"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB_false","testTable"),is(false));
        assertThat(privilegePath2.containsTargetPlace("testDB_false","testTable_false"),is(false));
        assertThat(privilegePath2.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB.testTable_false"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB_false.testTable"),is(false));
        assertThat(privilegePath2.containsTargetPlace("testDB_false.testTable_false"),is(false));

        assertThat(privilegePath3.containsTargetPlace("testDB","testTable"),is(true));
        assertThat(privilegePath3.containsTargetPlace("testDB","testTable_false"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false","testTable"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false","testTable_false"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB.testTable"),is(true));
        assertThat(privilegePath3.containsTargetPlace("testDB.testTable_false"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false.testTable"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false.testTable_false"),is(false));

        assertThat(privilegePath4.containsTargetPlace("testDB","testTable"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB","testTable_false"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false","testTable"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false","testTable_false"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB.testTable"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB.testTable_false"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false.testTable"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false.testTable_false"),is(false));
        // column
        assertThat(privilegePath4.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilegePath4.containsTargetPlace("testDB","testTable","col1_false"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB.testTable.col1"),is(true));
        assertThat(privilegePath4.containsTargetPlace("testDB.testTable.col1_false"),is(false));

        assertThat(privilegePath1.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB","testTable","col_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB","testTable_false","col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB","testTable_false","col1_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false","testTable","col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false","testTable","col1_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false","testTable_false","col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB.testTable.col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB.testTable.col_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB.testTable_false.col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB.testTable_false.col1_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false.testTable.col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false.testTable.col1_false"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false.testTable_false.col1"),is(true));
        assertThat(privilegePath1.containsTargetPlace("testDB_false.testTable_false.col1_false"),is(true));

        assertThat(privilegePath2.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB","testTable","col_false"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB","testTable_false","col1"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB","testTable_false","col1_false"),is(true));
        assertThat(privilegePath2.containsTargetPlace("testDB_false","testTable","col1"),is(false));
        assertThat(privilegePath2.containsTargetPlace("testDB_false","testTable","col1_false"),is(false));
        assertThat(privilegePath2.containsTargetPlace("testDB_false","testTable_false","col1"),is(false));
        assertThat(privilegePath2.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(false));

        assertThat(privilegePath3.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilegePath3.containsTargetPlace("testDB","testTable","col_false"),is(true));
        assertThat(privilegePath3.containsTargetPlace("testDB","testTable_false","col1"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB","testTable_false","col1_false"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false","testTable","col1"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false","testTable","col1_false"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false","testTable_false","col1"),is(false));
        assertThat(privilegePath3.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(false));

        assertThat(privilegePath4.containsTargetPlace("testDB","testTable","col1"),is(true));
        assertThat(privilegePath4.containsTargetPlace("testDB","testTable","col_false"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB","testTable_false","col1"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB","testTable_false","col1_false"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false","testTable","col1"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false","testTable","col1_false"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false","testTable_false","col1"),is(false));
        assertThat(privilegePath4.containsTargetPlace("testDB_false","testTable_false","col1_false"),is(false));
    }
}
