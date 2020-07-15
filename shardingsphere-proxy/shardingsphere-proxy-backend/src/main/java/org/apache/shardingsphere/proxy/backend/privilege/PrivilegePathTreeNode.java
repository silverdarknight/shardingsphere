package org.apache.shardingsphere.proxy.backend.privilege;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.*;

@Getter(value = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PROTECTED)
public class PrivilegePathTreeNode {

    private String pathValue = "";

    private Boolean isRegNode = false;

    private Collection<PrivilegePathTreeNode> offspring = new HashSet<>();

    private Boolean containsStar = false;

    private int curHeight;

    public PrivilegePathTreeNode(){
        this.setPathValue("root");
        this.setCurHeight(0);
    }

    public PrivilegePathTreeNode(String pathValue, PrivilegePathTreeNode treeNode){
        this.setPathValue(pathValue);
        if(treeNode.curHeight>=3) throw new ShardingSphereException("Please input correct path and columns.");
        this.setCurHeight(treeNode.getCurHeight()+1);
        this.setIsRegNode(checkPathValueIsReg(pathValue));
    }

    protected Boolean isPath(String path){
        if(!getIsRegNode()) {
            return path.trim().equals(getPathValue());
        }
        else return likePath(path);
    }

    // not used yet
    private Boolean checkPathValueIsReg(String pathValue){
        return false;
    }

    // not used yet
    private Boolean likePath(String path){
        return true;
    }

    protected void addOffspring(String dbName, String tableName){
        // root node add db.*.*
        if(getCurHeight()!=0) throw new ShardingSphereException("Error: wrong path input.");
        dbName = dbName.trim();
        if(dbName.equals("*")) this.setContainsStar(true);
        else{
            PrivilegePathTreeNode dbNode = new PrivilegePathTreeNode(dbName, this);
            if(!offspringContainsPathValue(dbName)) {
                this.getOffspring().add(dbNode);
            }
            else {
                Iterator<PrivilegePathTreeNode> iterator = this.getOffspring().iterator();
                while (iterator.hasNext()){
                    PrivilegePathTreeNode node = iterator.next();
                    if(node.getPathValue().equals(dbName)){
                        dbNode = node;
                        break;
                    }
                }
            }
            // child node is db node
            tableName = tableName.trim();
            if(tableName.equals("*")) dbNode.setContainsStar(true);
            else {
                PrivilegePathTreeNode tableNode = new PrivilegePathTreeNode(tableName, dbNode);
                if (!dbNode.offspringContainsPathValue(tableName)) {// children are columns, if table
                    dbNode.getOffspring().add(tableNode);
                } else {
                    Iterator<PrivilegePathTreeNode> iterator = dbNode.getOffspring().iterator();
                    while (iterator.hasNext()) {
                        PrivilegePathTreeNode node = iterator.next();
                        if (node.getPathValue().equals(tableName)) {
                            tableNode = node;
                            break;
                        }
                    }
                }
                tableNode.setContainsStar(true);
            }
        }
    }

    protected void addOffspring(String dbName, String tableName, List<String> colNames){
        // root node add db.table.cols
        if(getCurHeight()!=0) throw new ShardingSphereException("Error: wrong path input.");
        dbName = dbName.trim();
        if(dbName.equals("*")) this.setContainsStar(true);
        else{
            PrivilegePathTreeNode child = new PrivilegePathTreeNode(dbName, this);
            if(!this.offspringContainsPathValue(dbName)) {
                this.getOffspring().add(child);
            }
            else {
                Iterator<PrivilegePathTreeNode> iterator = this.getOffspring().iterator();
                while (iterator.hasNext()){
                    PrivilegePathTreeNode node = iterator.next();
                    if(node.getPathValue().equals(dbName)){
                        child = node;
                        break;
                    }
                }
            }
            // child node is db node
            child.addOffspring(tableName, colNames);
        }
    }

    protected void addOffspring(String tableName, List<String> colNames){
        // db node add table.cols
        if(getCurHeight()!=1) throw new ShardingSphereException("Error: wrong path input.");
        tableName = tableName.trim();
        if(tableName.equals("*")) this.setContainsStar(true);
        else {
            PrivilegePathTreeNode child = new PrivilegePathTreeNode(tableName, this);
            if(!this.offspringContainsPathValue(tableName)) {// children are columns, if table
                this.getOffspring().add(child);
            }
            else {
                Iterator<PrivilegePathTreeNode> iterator = this.getOffspring().iterator();
                while (iterator.hasNext()){
                    PrivilegePathTreeNode node = iterator.next();
                    if(node.getPathValue().equals(tableName)){
                        child = node;
                        break;
                    }
                }
            }
            // child node is table node
            child.addOffspring(colNames);
        }
    }

    protected void addOffspring(List<String> colNames){
        // table add cols
        if(getCurHeight()!=2) throw new ShardingSphereException("Error: wrong path input.");
        // all colNames are contained in this table node. Cause error Msg.
        Iterator<String> iterator = colNames.iterator();
        Boolean causeErrorEveryContains = true;
        while (iterator.hasNext()){
            String colName = iterator.next().trim();
            if(colName.equals("*")) {
                if(!this.getContainsStar()) {
                    causeErrorEveryContains = false;
                    this.setContainsStar(true);
                }
            }
            else {
                PrivilegePathTreeNode child = new PrivilegePathTreeNode(colName,this);
                if(!this.offspringContainsPathValue(colName)){
                    this.getOffspring().add(child);
                    causeErrorEveryContains = false;
                }
            }
        }
        if(causeErrorEveryContains)
            throw new ShardingSphereException("Illegal GRANT/REVOKE command; please consult" +
                    " the manual to see which privilege can be used");
    }

    protected void removeOffspring(String dbName, String tableName){
        if(getCurHeight()!=0) throw new ShardingSphereException("Error: wrong path input.");
        dbName = dbName.trim();
        if(dbName.equals("*")) {
            if(!this.getContainsStar())
                throw new ShardingSphereException("There is no such grant defined");
            this.setContainsStar(false);
        }
        else {
            PrivilegePathTreeNode dbNode = new PrivilegePathTreeNode(dbName, this);
            if(!this.offspringContainsPathValue(dbName))
                throw new ShardingSphereException("There is no such grant defined");
            else {
                Iterator<PrivilegePathTreeNode> iterator = this.getOffspring().iterator();
                while (iterator.hasNext()){
                    PrivilegePathTreeNode node = iterator.next();
                    if(node.getPathValue().equals(dbName)){
                        dbNode = node;
                        break;
                    }
                }
            }

            tableName = tableName.trim();
            if(tableName.equals("*")) {
                if(!dbNode.getContainsStar())
                    throw new ShardingSphereException("There is no such grant defined");
                dbNode.setContainsStar(false);
            }
            else {
                PrivilegePathTreeNode tableNode = new PrivilegePathTreeNode(tableName, dbNode);
                if(!dbNode.offspringContainsPathValue(tableName))
                    throw new ShardingSphereException("There is no such grant defined");
                else {
                    Iterator<PrivilegePathTreeNode> iterator = dbNode.getOffspring().iterator();
                    while (iterator.hasNext()){
                        PrivilegePathTreeNode node = iterator.next();
                        if(node.getPathValue().equals(tableName)){
                            node.setContainsStar(false);
                            node.getOffspring().clear();
                            dbNode.getOffspring().remove(node);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void removeOffspring(String dbName, String tableName, List<String> colNames){
        // db node remove table.cols
        if(getCurHeight()!=0) throw new ShardingSphereException("Error: wrong path input.");
        dbName = dbName.trim();
        if(dbName.equals("*")) {
            if(!this.getContainsStar())
                throw new ShardingSphereException("There is no such grant defined");
            this.setContainsStar(false);
        }
        else {
            PrivilegePathTreeNode child = new PrivilegePathTreeNode(dbName, this);
            if(!this.offspringContainsPathValue(dbName)) {// children are db
                throw new ShardingSphereException("There is no such grant defined");
            }
            else {
                Iterator<PrivilegePathTreeNode> iterator = this.getOffspring().iterator();
                while (iterator.hasNext()){
                    PrivilegePathTreeNode node = iterator.next();
                    if(node.getPathValue().equals(dbName)){
                        child = node;
                        break;
                    }
                }
            }
            // child node is table node
            child.removeOffspring(tableName, colNames);
        }
    }

    protected void removeOffspring(String tableName, List<String> colNames){
        // db node remove table.cols
        if(getCurHeight()!=1) throw new ShardingSphereException("Error: wrong path input.");
        tableName = tableName.trim();
        if(tableName.equals("*")) {
            if(!this.getContainsStar())
                throw new ShardingSphereException("There is no such grant defined");
            this.setContainsStar(false);
        }
        else {
            PrivilegePathTreeNode child = new PrivilegePathTreeNode(tableName, this);
            if(!this.offspringContainsPathValue(tableName)) {// children are table
                throw new ShardingSphereException("There is no such grant defined");
            }
            else {
                Iterator<PrivilegePathTreeNode> iterator = this.getOffspring().iterator();
                while (iterator.hasNext()){
                    PrivilegePathTreeNode node = iterator.next();
                    if(node.getPathValue().equals(tableName)){
                        child = node;
                        break;
                    }
                }
            }
            // child node is table node
            child.removeOffspring(colNames);
        }
    }

    protected void removeOffspring(List<String> colNames){
        // table add cols
        if(getCurHeight()!=2) throw new ShardingSphereException("Error: wrong path input.");
        // all colNames are not contained in this table node. Cause error Msg.
        if(colNames.size() == 0) {
            throw new ShardingSphereException("Error: null input columns");
        }
        else{
            Iterator<String> iterator = colNames.iterator();
            Boolean causeErrorEveryNotContains = true;
            while (iterator.hasNext()){
                String colName = iterator.next().trim();
                if(colName.equals("*")) {
                    if(this.getContainsStar() || this.getOffspring().size()!=0)
                        causeErrorEveryNotContains = false;
                    this.setContainsStar(false);
                    this.getOffspring().clear();
                    break;
                }
                else {
                    PrivilegePathTreeNode child = new PrivilegePathTreeNode(colName,this);
                    if(this.offspringContainsPathValue(colName)){
                        this.getOffspring().remove(child);
                        causeErrorEveryNotContains = false;
                    }
                }
            }
            if(causeErrorEveryNotContains)
                throw new ShardingSphereException("There is no such grant defined");
        }
    }

    protected Boolean offspringContainsPathValue(String pathValue){
        Iterator<PrivilegePathTreeNode> iterator = getOffspring().iterator();
        while (iterator.hasNext()){
            if(iterator.next().getPathValue().equals(pathValue.trim())) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivilegePathTreeNode that = (PrivilegePathTreeNode) o;

        HashSet<PrivilegePathTreeNode> thisOffspring = new HashSet<>()
                , thatOffspring = new HashSet<>();
        Iterator<PrivilegePathTreeNode> iterator = offspring.iterator();
        while (iterator.hasNext()){
            thisOffspring.add(iterator.next());
        }
        iterator = that.offspring.iterator();
        while (iterator.hasNext()){
            thatOffspring.add(iterator.next());
        }

        return curHeight == that.curHeight &&
                Objects.equals(pathValue, that.pathValue) &&
                Objects.equals(thisOffspring, thatOffspring) &&
                Objects.equals(containsStar, that.containsStar);
    }

    @Override
    public int hashCode() {
        HashSet<PrivilegePathTreeNode> newOffspring = new HashSet<>();
        Iterator<PrivilegePathTreeNode> iterator = offspring.iterator();
        while (iterator.hasNext()){
            newOffspring.add(iterator.next());
        }
        return Objects.hash(pathValue, newOffspring, containsStar, curHeight);
    }
}
