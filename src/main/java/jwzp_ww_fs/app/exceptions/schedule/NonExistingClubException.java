package jwzp_ww_fs.app.exceptions.schedule;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class NonExistingClubException extends ScheduleException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("401", "Referred club does not exist.");
    }
}
