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
    public void grantTest(){
        PrivilegePathTree tree = new PrivilegePathTree();
        List<String> cols = new LinkedList<>();
        tree.grantPath("db1","table1", cols);
        cols.add("col1");cols.add("col2");
        tree.grantPath("db1","table2", cols);
        tree.grantPath("db2","*");
        tree.grantPath("db3","table1");
        tree.grantPath("db4");
        tree.grantPath("*");
    }
}