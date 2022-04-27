package jwzp_ww_fs.app.exceptions.club;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class ProtrudingEventException extends ClubException{
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("101", "Club could not be modified because some event would protrude.");
    }
}
