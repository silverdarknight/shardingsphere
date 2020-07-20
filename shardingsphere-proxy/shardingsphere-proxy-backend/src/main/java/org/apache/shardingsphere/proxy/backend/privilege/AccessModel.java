package org.apache.shardingsphere.proxy.backend.privilege;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.privilege.common.PrivilegeActionType;
import org.apache.shardingsphere.proxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserInformation;
import org.apache.shardingsphere.proxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.config.yaml.YamlAccessModel;
import org.apache.shardingsphere.proxy.config.yaml.YamlPrivilegeConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlUserPrivilegeConfiguration;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Getter
public class AccessModel implements AccessExecutorWrapper, Serializable {

    private static final long serialVersionUID = -6691230884488440405L;

    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, UserInformation> userInformationMap = new HashMap<>();

    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, UserPrivilege> usersPrivilege = new HashMap<>();

    @Setter(value = AccessLevel.PRIVATE)
    private Collection<String> invalidUserGroup = new HashSet<>();

    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, RolePrivilege> rolesPrivileges = new HashMap<>();

    private final ReentrantReadWriteLock infoLock = new ReentrantReadWriteLock()
            , invalidGroupLock = new ReentrantReadWriteLock()
            , userPrivilegesLock = new ReentrantReadWriteLock()
            , rolePrivilegesLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock infoReadLock = infoLock.readLock()
            , invalidGroupReadLock = invalidGroupLock.readLock()
            , userPrivilegeReadLock = userPrivilegesLock.readLock()
            , rolePrivilegeReadLock = rolePrivilegesLock.readLock();

    private final ReentrantReadWriteLock.WriteLock infoWriteLock = infoLock.writeLock()
            , invalidGroupWriteLock = invalidGroupLock.writeLock()
            , userPrivilegeWriteLock = userPrivilegesLock.writeLock()
            , rolePrivilegeWriteLock = rolePrivilegesLock.writeLock();

    public AccessModel(YamlAccessModel yamlAccessModel){
        // role privileges
        Iterator<Map.Entry<String, YamlPrivilegeConfiguration>> roleIterator =  yamlAccessModel.getRoleList().entrySet().iterator();
        while (roleIterator.hasNext()){
            Map.Entry<String, YamlPrivilegeConfiguration> kv = roleIterator.next();
            RolePrivilege tmpRolePrivilege = new RolePrivilege(kv.getKey());
            tmpRolePrivilege.constructModel(kv.getValue());
            this.addRole(tmpRolePrivilege);
        }
        Iterator<Map.Entry<String, YamlUserPrivilegeConfiguration>> userIterator =  yamlAccessModel.getUserList().entrySet().iterator();
        while (userIterator.hasNext()){
            Map.Entry<String, YamlUserPrivilegeConfiguration> kv = userIterator.next();
            YamlUserPrivilegeConfiguration curConfig = kv.getValue();
            String userName = kv.getKey();
            // user information
            UserInformation tmpUserInformation = this.addUser(userName,curConfig.getPassword());
            this.getUserInformationMap().put(userName,tmpUserInformation);
            try {
                // user privileges
                UserPrivilege tmpUserPrivilege = new UserPrivilege();
                Iterator<String> roleNamesIterator = curConfig.getRoles().iterator();
                while (roleNamesIterator.hasNext()){
                    String roleName = roleNamesIterator.next();
                    if(rolesPrivileges.containsKey(roleName))
                        tmpUserPrivilege.grant(roleName);
                }
                tmpUserPrivilege.constructModel(curConfig.getPrivileges());
                this.addUserPrivilege(tmpUserInformation, tmpUserPrivilege);
            }
            catch (Exception e){
                //
            }
        }
        // invalid group
        this.getInvalidUserGroup().addAll(yamlAccessModel.getInvalidGroup());
    }

    public void updateInformation(Map<String, UserInformation> userInformationMap){
        System.out.println("be updated!");
        try {
            infoWriteLock.lock();
            this.setUserInformationMap(userInformationMap);
        }
        catch (Exception e){
            throw new ShardingSphereException("update access model failed");
        }
        finally {
            infoWriteLock.unlock();
        }
    }

    public void updateUsersPrivilege(Map<String, UserPrivilege> userPrivilegeMap){
        System.out.println("be updated!");
        try {
            userPrivilegeWriteLock.lock();
            this.setUsersPrivilege(userPrivilegeMap);
        }
        catch (Exception e){
            throw new ShardingSphereException("update access model failed");
        }
        finally {
            userPrivilegeWriteLock.unlock();
        }
    }

    public void updateInvalidGroup(Collection<String> invalidUserGroup){
        System.out.println("be updated!");
        try {
            invalidGroupWriteLock.lock();
            this.setInvalidUserGroup(invalidUserGroup);
        }
        catch (Exception e){
            throw new ShardingSphereException("update access model failed");
        }
        finally {
            invalidGroupWriteLock.unlock();
        }
    }

    public void updateRolePrivileges(Map<String, RolePrivilege> rolePrivilegeMap){
        System.out.println("be updated!");
        try {
            rolePrivilegeWriteLock.lock();
            this.setRolesPrivileges(rolePrivilegeMap);
        }
        catch (Exception e){
            throw new ShardingSphereException("update access model failed");
        }
        finally {
            rolePrivilegeWriteLock.unlock();
        }
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        return bos.toByteArray();
    }

    public byte[] informationToBytes() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getUserInformationMap());
        oos.flush();
        return bos.toByteArray();
    }

    public byte[] invalidGroupToBytes() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getInvalidUserGroup());
        oos.flush();
        return bos.toByteArray();
    }

    public byte[] rolePrivilegesToBytes() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getRolesPrivileges());
        oos.flush();
        return bos.toByteArray();
    }

    public byte[] usersPrivilegeToBytes() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(getUsersPrivilege());
        oos.flush();
        return bos.toByteArray();
    }

    public static AccessModel deserialize(byte[] serializeData)
            throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        AccessModel accessModel = (AccessModel) ois.readObject();
        return accessModel;
    }

    public static Map<String, UserInformation> deserializeUserInformation(byte[] serializeData)
            throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Map<String, UserInformation> model = (Map<String, UserInformation>) ois.readObject();
        return model;
    }

    public static Collection<String> deserializeInvalidGroup(byte[] serializeData)
            throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Collection<String> model = (Collection<String>) ois.readObject();
        return model;
    }

    public static Map<String, RolePrivilege> deserializeRolePrivileges(byte[] serializeData)
            throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Map<String, RolePrivilege> model = (Map<String, RolePrivilege>) ois.readObject();
        return model;
    }

    public static Map<String, UserPrivilege> deserializeUsersPrivilege(byte[] serializeData)
            throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(serializeData);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Map<String, UserPrivilege> model = (Map<String, UserPrivilege>) ois.readObject();
        return model;
    }

    private Boolean containsUser(String userName){
        return userInformationMap.containsKey(userName);
    }

    private Boolean containsRole(String roleName){
        roleName = roleName.trim();
        return this.getRolesPrivileges().containsKey(roleName);
    }

    private UserInformation getUser(String userName){
        if(!this.getUserInformationMap().containsKey(userName))
            throw new ShardingSphereException("No such user named :" + userName);
        return this.getUserInformationMap().get(userName);
    }

    private UserPrivilege getUserPrivilege(String userName){
        UserInformation userInformation = this.getUser(userName);
        UserPrivilege userPrivilege = this.getUsersPrivilege().get(userInformation);
        if(userPrivilege == null)
            throw new ShardingSphereException("User named :" + userName + "has no privilege granted");
        else
            return userPrivilege;
    }

    private RolePrivilege getRolePrivilege(String roleName){
        roleName = roleName.trim();
        if(this.containsRole(roleName))
            return this.getRolesPrivileges().get(roleName);
        throw new ShardingSphereException("No such role named :" + roleName);
    }

    private UserInformation addUser(String userName, String password){
        if(this.containsUser(userName))
            throw new ShardingSphereException("Already has a user called : " + userName);
        UserInformation userInformation = new UserInformation(userName, password);
        this.getUserInformationMap().put(userName, userInformation);
        return userInformation;
    }

    private UserPrivilege addUserPrivilege(UserInformation userInformation, UserPrivilege userPrivilege){
        this.getUsersPrivilege().put(userInformation.getUserName(), userPrivilege);
        return userPrivilege;
    }

    private RolePrivilege addRole(RolePrivilege rolePrivilege){
        if(!this.getRolesPrivileges().containsKey(rolePrivilege.getRoleName())){
            this.getRolesPrivileges().put(rolePrivilege.getRoleName()
                    , rolePrivilege);
            return rolePrivilege;
        }
        throw new ShardingSphereException("Already has a role called : " + rolePrivilege.getRoleName());
    }

    @Override
    public void createUser(String byUserName, String userName, String password) {
        if(checkHavePermission(byUserName, PrivilegeAction.CREATE)){
            UserInformation information = new UserInformation(userName, password);
            if(!this.getUserInformationMap().containsKey(userName)){
                this.getUserInformationMap().put(userName, information);
            }
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void createRole(String byUserName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.CREATE)){
            RolePrivilege information = new RolePrivilege(roleName);
            if(!this.getRolesPrivileges().containsKey(roleName)){
                this.getRolesPrivileges().put(roleName, information);
            }
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void removeUser(String byUserName, String userName) {
        if(checkHavePermission(byUserName, PrivilegeAction.REMOVE)){
            getInvalidUserGroup().remove(userName);
            getUserInformationMap().remove(userName);
            getUsersPrivilege().remove(userName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void removeRole(String byUserName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.REMOVE)){
            RolePrivilege targetRole = getRolePrivilege(roleName);
            // users revoke role
            Iterator<Map.Entry<String, UserPrivilege>> userPrivilegeIterator = getUsersPrivilege()
                    .entrySet().iterator();
            while (userPrivilegeIterator.hasNext()){
                Map.Entry<String, UserPrivilege> kv = userPrivilegeIterator.next();
                try {
                    kv.getValue().revoke(targetRole.getRoleName());
                }
                catch (Exception e){
                    //
                }
            }
            // remove role
            getRolesPrivileges().remove(roleName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void disableUser(String byUserName, String userName) {
        if(checkHavePermission(byUserName, PrivilegeAction.DISABLE)){
            getInvalidUserGroup().add(userName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName,
                                      String userName,
                                      String privilegeType,
                                      String database,
                                      String table,
                                      List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            if(!getUsersPrivilege().containsKey(userName)) return false;
            Iterator<String> iterator = column.iterator();
            while (iterator.hasNext()){
                if(!checkUserPrivilege(byUserName,userName,privilegeType,database,table,iterator.next()))
                    return false;
            }
            return true;
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName,
                                      String userName,
                                      String privilegeType,
                                      String database,
                                      String table,
                                      String column) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            if(!getUsersPrivilege().containsKey(userName)) return false;
            boolean selfCheck = getUsersPrivilege().get(userName).checkPrivilege(privilegeType,database,table,column);
            if (selfCheck) return true;
            else{
                List<String> selfRoles = getUsersPrivilege().get(userName).getRolesName();
                Iterator<String> iterator = selfRoles.iterator();
                while (iterator.hasNext()){
                    String curRole = iterator.next();
                    RolePrivilege curRoleModel = getRolesPrivileges().get(curRole);
                    if(curRoleModel.checkPrivilege(privilegeType,database,table,column)) return true;
                }
                return false;
            }
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public Boolean checkUserPrivilege(String byUserName,
                                      String userName,
                                      String privilegeType,
                                      String database,
                                      String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.CHECK)){
            if(!getUsersPrivilege().containsKey(userName)) return false;
            boolean selfCheck = getUsersPrivilege().get(userName).checkPrivilege(privilegeType,database,table);
            if (selfCheck) return true;
            else{
                List<String> selfRoles = getUsersPrivilege().get(userName).getRolesName();
                Iterator<String> iterator = selfRoles.iterator();
                while (iterator.hasNext()){
                    String curRole = iterator.next();
                    RolePrivilege curRoleModel = getRolesPrivileges().get(curRole);
                    if(curRoleModel.checkPrivilege(privilegeType,database,table)) return true;
                }
                return false;
            }
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantUser(String byUserName, String userName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            createUserPrivilegeIfNotExist(userName);
            getUsersPrivilege().get(userName).grant(roleName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getRolesPrivileges().get(roleName).grant(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getRolesPrivileges().get(roleName).grant(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void grantRole(String byUserName, String roleName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.GRANT)){
            getRolesPrivileges().get(roleName).grant(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getUsersPrivilege().get(userName).revoke(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getUsersPrivilege().get(userName).revoke(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getUsersPrivilege().get(userName).revoke(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeUser(String byUserName, String userName, String roleName) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getUsersPrivilege().get(userName).revoke(roleName);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table, List<String> column) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getRolePrivilege(roleName).revoke(privilegeType,database,table,column);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String database, String table) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getRolePrivilege(roleName).revoke(privilegeType,database,table);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    @Override
    public void revokeRole(String byUserName, String roleName, String privilegeType, String information) {
        if(checkHavePermission(byUserName, PrivilegeAction.REVOKE)){
            getRolePrivilege(roleName).revoke(privilegeType,information);
        }
        else throw new ShardingSphereException("You do not have this permission.");
    }

    private Boolean checkHavePermission(String byUser, String actionType){
        if(getInvalidUserGroup().contains(byUser)) return false;
        return true;
    }

    private void createUserPrivilegeIfNotExist(String userName){
        if(!getUsersPrivilege().containsKey(userName)){
            if(!getUserInformationMap().containsKey(userName))
                throw new ShardingSphereException("No such user called :" + userName);
            else {
                getUsersPrivilege().put(userName,new UserPrivilege());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessModel that = (AccessModel) o;
        return Objects.equals(userInformationMap, that.userInformationMap) &&
                Objects.equals(usersPrivilege, that.usersPrivilege) &&
                Objects.equals(invalidUserGroup, that.invalidUserGroup) &&
                Objects.equals(rolesPrivileges, that.rolesPrivileges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userInformationMap, usersPrivilege, invalidUserGroup, rolesPrivileges);
    }

    public Boolean doAction(PrivilegeAction action){
        String byUser = action.getByUser();
        if(action.getActionType().equals(PrivilegeAction.CREATE)){
            String name = action.getName();
            if(action.getIsUser()){
                String password = action.getPassword();
                try {
                    infoWriteLock.lock();
                    createUser(byUser,name,password);
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    infoWriteLock.unlock();
                }
            }
            else{
                try {
                    infoWriteLock.lock();
                    createRole(byUser,name);
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    infoWriteLock.unlock();
                }
            }
        }
        else if(action.getActionType().equals(PrivilegeAction.REMOVE)){
            String name = action.getName();
            if(action.getIsUser()){
                try {
                    userPrivilegeWriteLock.lock();
                    removeUser(byUser,name);
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    userPrivilegeWriteLock.unlock();
                }
            }
            else{
                try {
                    rolePrivilegeWriteLock.lock();
                    removeRole(byUser,name);
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    rolePrivilegeWriteLock.unlock();
                }
            }
        }
        else if(action.getActionType().equals(PrivilegeAction.DISABLE)){
            String name = action.getName();
            try {
                invalidGroupWriteLock.lock();
                disableUser(byUser,name);
            }
            catch (Exception e){
                throw new ShardingSphereException(e.getMessage());
            }
            finally {
                invalidGroupWriteLock.unlock();
            }
        }
        else if(action.getActionType().equals(PrivilegeAction.CHECK)){
            String name = action.getName()
                    , privilegeType = action.getPrivilegeType()
                    , dbName = action.getDbName()
                    , tableName = action.getTableName();
            List<String> cols = action.getColumns();
            boolean selfCheckAns;
            if(cols==null){
                try {
                    userPrivilegeReadLock.lock();
                    selfCheckAns = checkUserPrivilege(byUser,name,privilegeType,dbName,tableName);
                    return selfCheckAns;
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    userPrivilegeReadLock.unlock();
                }
            }
            else{
                try {
                    userPrivilegeReadLock.lock();
                    selfCheckAns = checkUserPrivilege(byUser,name,privilegeType,dbName,tableName,cols);
                    return selfCheckAns;
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    userPrivilegeReadLock.unlock();
                }
            }
        }
        else if(action.getActionType().equals(PrivilegeAction.GRANT)){
            String name = action.getName();
            if(action.getRoleName() != null){
                try {
                    userPrivilegeWriteLock.lock();
                    grantUser(byUser,name,action.getRoleName());
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    userPrivilegeWriteLock.unlock();
                }
            }
            else{
                String privilegeType = action.getPrivilegeType()
                        , dbName = action.getDbName()
                        , tableName = action.getTableName();
                List<String> cols = action.getColumns();
                if(cols==null){
                    if(action.getIsUser()) {
                        try {
                            userPrivilegeWriteLock.lock();
                            grantUser(byUser,name,privilegeType,dbName,tableName);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            userPrivilegeWriteLock.unlock();
                        }
                    }
                    else {
                        try {
                            rolePrivilegeWriteLock.lock();
                            grantRole(byUser,name,privilegeType,dbName,tableName);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            rolePrivilegeWriteLock.unlock();
                        }
                    }
                }
                else{
                    if(action.getIsUser()) {
                        try {
                            userPrivilegeWriteLock.lock();
                            grantUser(byUser,name,privilegeType,dbName,tableName,cols);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            userPrivilegeWriteLock.unlock();
                        }
                    }
                    else {
                        try {
                            rolePrivilegeWriteLock.lock();
                            grantRole(byUser,name,privilegeType,dbName,tableName,cols);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            rolePrivilegeWriteLock.unlock();
                        }
                    }
                }
            }
        }
        else if(action.getActionType().equals(PrivilegeAction.REVOKE)){
            String name = action.getName();
            if(action.getRoleName() != null){
                try {
                    userPrivilegeWriteLock.lock();
                    revokeUser(byUser,name,action.getRoleName());
                }
                catch (Exception e){
                    throw new ShardingSphereException(e.getMessage());
                }
                finally {
                    userPrivilegeWriteLock.unlock();
                }
            }
            else {
                String privilegeType = action.getPrivilegeType()
                        , dbName = action.getDbName()
                        , tableName = action.getTableName();
                List<String> cols = action.getColumns();
                if(cols==null) {
                    if(action.getIsUser()) {
                        try {
                            userPrivilegeWriteLock.lock();
                            revokeUser(byUser,name,privilegeType,dbName,tableName);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            userPrivilegeWriteLock.unlock();
                        }
                    }
                    else {
                        try {
                            rolePrivilegeWriteLock.lock();
                            revokeRole(byUser,name,privilegeType,dbName,tableName);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            rolePrivilegeWriteLock.unlock();
                        }
                    }
                }
                else {
                    if(action.getIsUser()) {
                        try {
                            userPrivilegeWriteLock.lock();
                            revokeUser(byUser,name,privilegeType,dbName,tableName,cols);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            userPrivilegeWriteLock.unlock();
                        }
                    }
                    else {
                        try {
                            rolePrivilegeWriteLock.lock();
                            revokeRole(byUser,name,privilegeType,dbName,tableName,cols);
                        }
                        catch (Exception e){
                            throw new ShardingSphereException(e.getMessage());
                        }
                        finally {
                            rolePrivilegeWriteLock.unlock();
                        }
                    }
                }
            }
        }
        else{
            throw new ShardingSphereException("privilege action type error.");
        }
        return true;
    }
}
