package sugar.free.sightremote.utils;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

public class CrashlyticsUtil {

    public static void logExceptionWithCallStackTrace(Exception e) {
        Exception exception = new Exception(null, e);
        StackTraceElement[] realStackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> stackTrace = new ArrayList<>();
        for (StackTraceElement stackTraceElement : realStackTrace)
            if (!stackTraceElement.getClassName().equals(CrashlyticsUtil.class.getName()))
                stackTrace.add(stackTraceElement);
        exception.setStackTrace(stackTrace.toArray(new StackTraceElement[0]));
        Crashlytics.logException(e);
    }

}
