package jwzp_ww_fs.app.Exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventNotInOpeningHoursException extends GymException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("4", "Events within a club have to be within the club's opening hours");
    }
}