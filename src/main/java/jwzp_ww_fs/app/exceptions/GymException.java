package jwzp_ww_fs.app.exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public abstract class GymException extends RuntimeException {
    public abstract ExceptionInfo getErrorInfo();
}
