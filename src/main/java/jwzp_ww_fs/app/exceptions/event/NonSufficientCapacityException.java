package jwzp_ww_fs.app.exceptions.event;

import jwzp_ww_fs.app.models.ExceptionInfo;

public class NonSufficientCapacityException extends EventException {
    @Override
    public ExceptionInfo getErrorInfo() {
        return new ExceptionInfo("304", "Event does not have sufficient capacity.");
    }
}
