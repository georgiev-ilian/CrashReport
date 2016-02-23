package my.crashreport;

import android.app.Application;
import android.util.Log;

/**
 * Created by Ilian Georgiev.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        ReportDataCollector.ExternalData externalData = new ReportDataCollector.ExternalData();
        Thread.setDefaultUncaughtExceptionHandler(
                new MyUncaughtExceptionHandler(this,
                        Thread.getDefaultUncaughtExceptionHandler(),
                        externalData, new Sender() {
                    @Override
                    public boolean send(String content) {
                        Log.d("MyApplication", "send: " + content);
                        return true;
                    }
                }));

        super.onCreate();
    }
}
