package org.apache.shardingsphere.proxy.backend.privilege;

import java.util.List;

public interface PrivilegeExecutorWrapper {

    public void setNextAction(PrivilegeAction action);

    public void runAction(PrivilegeAction action);

    public void redoAction(PrivilegeAction action);

    public boolean checkAction(PrivilegeAction action);

    public void otherAction(PrivilegeAction action);
}
