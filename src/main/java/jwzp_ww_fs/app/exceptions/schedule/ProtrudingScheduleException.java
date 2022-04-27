package jwzp_ww_fs.app.exceptions.schedule;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class ProtrudingScheduleException extends ScheduleException{
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("404", "Schedule is not within club opening hours.");
    }
}
