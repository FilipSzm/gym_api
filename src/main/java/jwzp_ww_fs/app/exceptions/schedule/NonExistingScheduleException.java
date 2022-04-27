package jwzp_ww_fs.app.exceptions.schedule;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class NonExistingScheduleException extends ScheduleException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("406", "Schedule with such ID was not found.");
    }
}
