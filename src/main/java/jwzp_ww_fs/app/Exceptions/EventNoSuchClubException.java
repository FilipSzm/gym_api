package jwzp_ww_fs.app.Exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventNoSuchClubException extends GymException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("2", "When creating an event, referred club have to exist");
    }
}