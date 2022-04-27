package jwzp_ww_fs.app.exceptions.coach;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventAssociatedWithCoachException extends CoachException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("201", "Coach designed to be deleted is assigned to some events.");
    }
}
