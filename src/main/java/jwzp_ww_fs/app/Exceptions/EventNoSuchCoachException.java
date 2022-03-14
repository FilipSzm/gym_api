package jwzp_ww_fs.app.Exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventNoSuchCoachException extends GymException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("3", "When creating an event, referred coach have to exist");
    }
}