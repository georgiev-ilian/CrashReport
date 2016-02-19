package my.crashreport;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Ilian Georgiev.
 */
public class ReportDataCollector {

    private static final int PHONE_MODEL_INDEX = 0;
    private static final int ANDROID_VERSION_INDEX = 1;
    private static final int SDK_INT_INDEX = 2;
    private static final int PACKAGE_NAME_INDEX = 3;
    private static final int APP_VERSION_CODE_INDEX = 4;
    private static final int APP_VERSION_NAME_INDEX = 5;
    private static final int CRASH_TIME_INDEX = 6;
    private static final int APP_START_TIME_INDEX = 7;
    private static final int STACK_TRACE_INDEX = 8;
    private static final int STACK_TRACE_HASH_INDEX = 9;
    private static final int PRODUCT_INDEX = 10;
    private static final int BRAND_INDEX = 11;
    private static final int DEVICE_ID_INDEX = 12;
    private static final int ACCOUNTS_INDEX = 13;


    public JSONObject collect(Context context, Throwable throwable, ExternalData externalData) {
        JSONObject jsonObj = null;

        try {
            jsonObj = new JSONObject();

            String packageName = context.getPackageName();

            jsonObj.put(String.valueOf(PHONE_MODEL_INDEX), Build.MODEL);
            jsonObj.put(String.valueOf(ANDROID_VERSION_INDEX), Build.VERSION.RELEASE);
            jsonObj.put(String.valueOf(SDK_INT_INDEX), String.valueOf(Build.VERSION.SDK_INT));
            jsonObj.put(String.valueOf(PACKAGE_NAME_INDEX), packageName);

            jsonObj.put(String.valueOf(APP_VERSION_CODE_INDEX), "");
            jsonObj.put(String.valueOf(APP_VERSION_NAME_INDEX), "");

            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                    if (packageInfo != null) {
                        jsonObj.put(String.valueOf(APP_VERSION_CODE_INDEX),
                                String.valueOf(packageInfo.versionCode));
                        jsonObj.put(String.valueOf(APP_VERSION_NAME_INDEX), packageInfo.versionName);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // ignore and continue without version strings
                }
            }

            jsonObj.put(String.valueOf(CRASH_TIME_INDEX),
                    String.valueOf(System.currentTimeMillis()));
            jsonObj.put(String.valueOf(APP_START_TIME_INDEX),
                    String.valueOf(externalData.appStartTime));

            jsonObj.put(String.valueOf(STACK_TRACE_INDEX), getStackTrace(throwable));
            jsonObj.put(String.valueOf(STACK_TRACE_HASH_INDEX), getStackTraceHash(throwable));

            jsonObj.put(String.valueOf(PRODUCT_INDEX), Build.PRODUCT);
            jsonObj.put(String.valueOf(BRAND_INDEX), Build.BRAND);

            jsonObj.put(String.valueOf(DEVICE_ID_INDEX), externalData.deviceId);
            jsonObj.put(String.valueOf(ACCOUNTS_INDEX), externalData.accounts);


        } catch (JSONException e) {
            // ignore
        }

        return jsonObj;
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

    public static class ExternalData {
        public long appStartTime;
        public String deviceId;
        public String accounts;
    }
}
