package jwzp_ww_fs.app.exceptions.event;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class ConcludedEventException extends EventException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("303", "Event already has been concluded.");
    }
}
