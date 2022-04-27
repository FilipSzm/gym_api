package jwzp_ww_fs.app.exceptions.event;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class AlreadyAssignedCoachException extends EventException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("305", "Given coach is already assigned to a different event.");
    }
}
