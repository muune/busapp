package teamdoppelganger.smarterbus.util.common;

import android.util.Log;

import teamdoppelganger.smarterbus.common.Constants;


public class Debug {

    static final String TAG = "SmarterBus";

    /**
     * Log Level Error
     **/
    public static final void e(String message) {
        if (Constants.DEBUG_MODE) Log.e(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Warning
     **/
    public static final void w(String message) {
        if (Constants.DEBUG_MODE) Log.w(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Information
     **/
    public static final void i(String message) {
        if (Constants.DEBUG_MODE) Log.i(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Debug
     **/
    public static final void d(String message) {
        if (Constants.DEBUG_MODE) Log.d(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Verbose
     **/
    public static final void v(String message) {
        if (Constants.DEBUG_MODE) Log.v(TAG, buildLogMsg(message));
    }

    public static String buildLogMsg(String message) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]  ");
        sb.append(message);
        return sb.toString();
    }


    public static void Log(String log) {
        if (Constants.DEBUG_MODE)
            Log.i("Log", "Log = " + log);
    }

    public static void HooLog(String title, Object value) {
        if (!Constants.DEBUG_MODE) return;

        String msg = title;

        if (value != null)
            msg += " : " + value.toString();

        Log.i("Hoo", msg);
    }

    public static void AdLog(Class location, String title, Object value) {
        if (!Constants.DEBUG_MODE) return;

        String className = location == null ? "" : location.getSimpleName() + " : ";
        String name = title == null ? "" : title + " = ";
        String msg = value == null ? "" : value.toString();

        Log.i("AdLog", className + name + msg);
    }
}
