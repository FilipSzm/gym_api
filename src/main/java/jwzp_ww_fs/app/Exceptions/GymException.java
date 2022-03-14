package jwzp_ww_fs.app.Exceptions;

import jwzp_ww_fs.app.models.ExceptionInfo;

public abstract class GymException extends Exception {
    public abstract ExceptionInfo getErrorInfo();
}
