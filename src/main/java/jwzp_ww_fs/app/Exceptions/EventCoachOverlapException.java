package jwzp_ww_fs.app.Exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventCoachOverlapException extends GymException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("1", "One coach can be assigned to only one event at any particular time");
    }
}