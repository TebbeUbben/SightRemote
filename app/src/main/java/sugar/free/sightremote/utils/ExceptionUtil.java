package sugar.free.sightremote.utils;

public class ExceptionUtil {

    public static Exception wrapException(Exception e) {
        Exception exception = new Exception(null, e);
        e.setStackTrace(Thread.currentThread().getStackTrace());
        return exception;
    }

}
