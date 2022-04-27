package jwzp_ww_fs.app.exceptions.event;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class FilledEventException extends EventException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("302", "Event capacity has been reached.");
    }
}
