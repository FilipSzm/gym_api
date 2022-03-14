package jwzp_ww_fs.app.Exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventHoursInClubException extends GymException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("5", "When modifying club's opening hours, events cannot stand out - such change cannot be performed.");
    }
}
