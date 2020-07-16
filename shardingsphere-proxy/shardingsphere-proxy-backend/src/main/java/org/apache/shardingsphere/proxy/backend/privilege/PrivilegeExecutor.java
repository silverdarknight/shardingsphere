package org.apache.shardingsphere.proxy.backend.privilege;

import java.util.List;

public class PrivilegeExecutor  implements PrivilegeExecutorWrapper{

    private AccessModel accessModel;

    private ZKPrivilegeWatcher zkPrivilegeWatcher;

    private PrivilegeAction waitingAction;

    @Override
    public void setNextAction(PrivilegeAction action) {

    }

    @Override
    public void runAction(PrivilegeAction action) {

    }

    @Override
    public void redoAction(PrivilegeAction action) {

    }

    @Override
    public boolean checkAction(PrivilegeAction action) {
        return false;
    }

    @Override
    public void otherAction(PrivilegeAction action) {

    }
}
