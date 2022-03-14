package jwzp_ww_fs.app.Exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventTooLongException extends GymException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("6", "Event cannot be longer than 24h");
    }
}