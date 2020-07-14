package org.apache.shardingsphere.proxy.backend.privilege;

import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class PrivilegeTreeTest {

    @Test
    public void constructTest(){
        PrivilegePathTree tree = new PrivilegePathTree();
        assertThat(tree,instanceOf(PrivilegePathTree.class));
        assertThat(tree.getRoot().getOffspring().size(),is(0));
    }

    @Test
    public void grantTableTest(){
        PrivilegePathTree tree = new PrivilegePathTree();
        tree.grantPath("db1","table1");
        tree.grantPath("db1","table2");
        assertThat(tree.checkPath("db1","table1"), is(true));
        assertThat(tree.checkPath("db1","table2"), is(true));
        assertThat(tree.checkPath("db1","error"), is(false));
        assertThat(tree.checkPath("db2","table1"), is(false));
        tree.grantPath("db2","*");
        assertThat(tree.checkPath("db2","error"), is(true));
        assertThat(tree.checkPath("error","error"), is(false));
        tree.grantPath("*","*");
        assertThat(tree.checkPath("error","error"), is(true));
    }

    @Test
    public void grantColumnTest(){
        PrivilegePathTree tree = new PrivilegePathTree();
        tree.grantPath("db1","table1");
        assertThat(tree.checkPath("db1","table1","any"), is(true));
        assertThat(tree.checkPath("db1","table2","any"), is(false));
        List<String> cols1 = new LinkedList<>(); cols1.add("col1"); cols1.add("col2");
        tree.grantPath("db1","table2", cols1);
        assertThat(tree.checkPath("db1","table2","col1"), is(true));
        assertThat(tree.checkPath("db1","table2","error"), is(false));
        assertThat(tree.checkPath("db1","table2", "col2"), is(true));
        assertThat(tree.checkPath("db1","error", "col1"), is(false));
        assertThat(tree.checkPath("error","table2", "col1"), is(false));
        cols1.add("*");
        tree.grantPath("db1","table2", cols1);
        assertThat(tree.checkPath("db1","table2", "error"), is(true));
        assertThat(tree.checkPath("db1","error", "col1"), is(false));
    }

    @Test
    public void revokeTableTest(){
        PrivilegePathTree tree = new PrivilegePathTree();
        tree.grantPath("db1","table1");
        // no such grant error
        // tree.revokePath("db1","*");
        tree.revokePath("db1","table1");
        tree.grantPath("db1","*");
        // no such grant error
        //tree.revokePath("db1","table1");
        tree.revokePath("db1","*");
        assertThat(tree.getRoot().getOffspring().size(),is(0));
        tree.grantPath("db1","table1");
        tree.grantPath("db1","table2");
        tree.revokePath("db1","table2");
        assertThat(tree.checkPath("db1","table1"), is(true));
        assertThat(tree.checkPath("db1","table2"), is(false));
        tree.grantPath("db1","*");
        tree.revokePath("db1","table1");
        assertThat(tree.checkPath("db1","table1"), is(true));
        assertThat(tree.checkPath("db1","error"), is(true));
        tree.revokePath("db1","*");
        assertThat(tree.checkPath("db1","table1"), is(false));
    }

    @Test
    public void revokeColumnTest(){
        PrivilegePathTree tree = new PrivilegePathTree();
        List<String> cols1 = new LinkedList<>(); cols1.add("*");
        tree.grantPath("db1","table1", cols1);
        List<String> testCols = new LinkedList<>();
        // no such grant error
        // testCols.add("col1");
        // tree.revokePath("db1", "table1", testCols);
        testCols = new LinkedList<>(); testCols.add("*");
        tree.revokePath("db1", "table1", testCols);
        assertThat(tree.getRoot().getOffspring().size(),is(0));
        testCols = new LinkedList<>(); testCols.add("col1"); testCols.add("col2");
        tree.grantPath("db1", "table1", testCols);
        testCols = new LinkedList<>(); testCols.add("col2");
        tree.revokePath("db1", "table1", testCols);
        assertThat(tree.checkPath("db1","table1","col1"),is(true));
        assertThat(tree.checkPath("db1","table1","col2"),is(false));
        testCols = new LinkedList<>(); testCols.add("*");
        tree.revokePath("db1", "table1", testCols);
        assertThat(tree.checkPath("db1","table1","col1"),is(false));
        assertThat(tree.getRoot().getOffspring().size(),is(0));
    }
}