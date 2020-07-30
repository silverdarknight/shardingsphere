package org.apache.shardingsphere.proxy.backend.privilege.common;

public class PrivilegeExceptions {

    public static String cannotActNodeAfterColumn = "can not add/remove child after column nodes."
            , alreadyHasPrivilege = "already have this privileges"
            , doNotHaveThisPrivilege = "do not have this privilege."
            , noSuchGrantDefined = "There is no such grant defined";
}
