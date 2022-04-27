package jwzp_ww_fs.app.exceptions.schedule;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class AlreadyAssignedCoachException extends ScheduleException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("403", "Given coach is already assigned in this particular time.");
    }
}
