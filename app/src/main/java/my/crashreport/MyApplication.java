package my.crashreport;

import android.app.Application;

/**
 * Created by Ilian Georgiev.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        ReportDataCollector.ExternalData externalData = new ReportDataCollector.ExternalData();
        Thread.setDefaultUncaughtExceptionHandler(
                new MyUncaughtExceptionHandler(this, externalData));

        super.onCreate();
    }
}
