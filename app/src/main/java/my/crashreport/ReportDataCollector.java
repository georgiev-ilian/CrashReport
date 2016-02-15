package my.crashreport;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Ilian Georgiev.
 */
public class ReportDataCollector {

    public static final String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";

    private final String[] data = new String[DATA_COUNT];

    private static final int PHONE_MODEL_INDEX = 0;
    private static final int ANDROID_VERSION_INDEX = 1;
    private static final int SDK_INT_INDEX = 2;
    private static final int PACKAGE_NAME_INDEX = 3;
    private static final int APP_VERSION_CODE = 4;
    private static final int APP_VERSION_NAME = 5;
    private static final int USER_CRASH_DATE = 6;
    private static final int STACK_TRACE = 7;
    private static final int STACK_TRACE_HASH = 8;
    private static final int DATA_COUNT = 9;


    public void collect(Context context, Throwable throwable) {
        data[PHONE_MODEL_INDEX] = Build.MODEL;
        data[ANDROID_VERSION_INDEX] = Build.VERSION.RELEASE;
        data[SDK_INT_INDEX] = String.valueOf(Build.VERSION.SDK_INT);
        data[PACKAGE_NAME_INDEX] = context.getPackageName();

        data[APP_VERSION_CODE] = "";
        data[APP_VERSION_NAME] = "";

        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(data[PACKAGE_NAME_INDEX], 0);
                if (packageInfo != null) {
                    data[APP_VERSION_CODE] = String.valueOf(packageInfo.versionCode);
                    data[APP_VERSION_NAME] = packageInfo.versionName;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // ignore and continue without version strings
            }
        }

        data[USER_CRASH_DATE] = getTimeString();

        data[STACK_TRACE] = getStackTrace(throwable);
        data[STACK_TRACE_HASH] = getStackTraceHash(throwable);
    }

    private String getTimeString() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT_STRING, Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();

        return format.format(calendar.get(Calendar.SECOND));
    }

    private String getStackTrace(Throwable th) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = th;

        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        final String stacktraceAsString = result.toString();
        printWriter.close();
        try {
            result.close();
        } catch (IOException e) {
            // ignore it
        }

        return stacktraceAsString;
    }

    private String getStackTraceHash(Throwable th) {
        final StringBuilder res = new StringBuilder();
        Throwable cause = th;

        while (cause != null) {
            final StackTraceElement[] stackTraceElements = cause.getStackTrace();

            for (final StackTraceElement e : stackTraceElements) {
                res.append(e.getClassName());
                res.append(e.getMethodName());
            }
            cause = cause.getCause();
        }

        return Integer.toHexString(res.toString().hashCode());
    }

    public void printToLogcat() {
        for (int i = 0; i < DATA_COUNT; i++) {
            Log.d("ReportDataCollector", "printToLogcat: " + data[i]);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < DATA_COUNT; i++) {
            builder.append(data[i]);
            builder.append("\n");
        }

        return builder.toString();
    }
}
