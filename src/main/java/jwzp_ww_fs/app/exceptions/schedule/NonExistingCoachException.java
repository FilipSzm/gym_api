package jwzp_ww_fs.app.exceptions.schedule;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class NonExistingCoachException extends ScheduleException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("402", "Referred coach does not exist.");
    }
}
