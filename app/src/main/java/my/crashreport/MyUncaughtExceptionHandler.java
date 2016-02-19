package my.crashreport;

import android.content.Context;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Ilian Georgiev.
 */
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    private final long appStartTime;

    public MyUncaughtExceptionHandler(Context context) {
        this.context = context;
        appStartTime = System.currentTimeMillis();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ReportDataCollector collector = new ReportDataCollector();
        ReportDataCollector.ExternalData externalData = new ReportDataCollector.ExternalData();
        externalData.appStartTime = appStartTime;

        collector.collect(context, ex, externalData);

        collector.printToLogcat();

        new ToastThread();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // ignore
        }

        Log.e("MyUncaught", "uncaughtException: " + ex.getMessage(), ex);

        android.os.Process.killProcess(Process.myPid());
        System.exit(10);
    }

    private class ToastThread implements Runnable {

        public ToastThread() {
            new Thread(this).start();
        }

        @Override
        public void run() {
            Looper.prepare();
            Toast.makeText(context,
                    "An error occurred. Error report has been sent.", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }
}
