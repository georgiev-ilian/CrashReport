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
                new MyUncaughtExceptionHandler(this, externalData, new Sender() {
                    @Override
                    public void send(String filename, String content, Listener listener) {
                        Log.d("MyApplication", "send: " + content);
                        listener.onReportSend(filename);
                    }
                }));

        super.onCreate();
    }
}
