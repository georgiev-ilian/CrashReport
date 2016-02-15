package crashreport.prototype.android.com.crashreport;

import android.os.Build;

/**
 * Created by Ilian Georgiev.
 */
public class ReportDataCollector {

    private final String[] data = new String[DATA_COUNT];

    private static final int PHONE_MODEL_INDEX = 0;
    private static final int ANDROID_VERSION_INDEX = 1;
    private static final int SDK_INT_INDEX = 2;
    private static final int PACKAGE_NAME = 3;
    private static final int DATA_COUNT = 3;

    public void collect() {
        data[PHONE_MODEL_INDEX] = Build.MODEL;
        data[ANDROID_VERSION_INDEX] = Build.VERSION.RELEASE;
        data[SDK_INT_INDEX] = String.valueOf(Build.VERSION.SDK_INT);
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
