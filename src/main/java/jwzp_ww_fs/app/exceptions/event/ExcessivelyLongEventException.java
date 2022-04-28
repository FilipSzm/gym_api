package jwzp_ww_fs.app.exceptions.event;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class ExcessivelyLongEventException extends EventException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("307", "Event cannot be longer than 24h");
    }
}
