package jwzp_ww_fs.app.exceptions.club;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class EventAssociatedWithClubException extends ClubException{
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("102", "Club designed to be deleted contains some events.");
    }
}
