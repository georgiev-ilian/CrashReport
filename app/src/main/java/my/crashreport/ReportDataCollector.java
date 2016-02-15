package my.crashreport;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

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
    private static final int DATA_COUNT = 7;

    public void collect(Context context) {
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

    }

    private String getTimeString() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT_STRING, Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();

        return format.format(calendar.get(Calendar.SECOND));
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
