package jwzp_ww_fs.app.exceptions.schedule;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class ExcessivelyLongScheduleException extends ScheduleException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("405", "Event cannot be longer than 24h");
    }
}
