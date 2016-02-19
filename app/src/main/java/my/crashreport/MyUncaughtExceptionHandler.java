package my.crashreport;

import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * Created by Ilian Georgiev.
 */
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String REPORTFILE_EXTENSION = ".crashreport";


    private final Context context;

    private final ReportDataCollector.ExternalData externalData;

    private final CrashReportFinder crashReportFinder;

    private final Sender sender;

    public MyUncaughtExceptionHandler(Context context,
                                      ReportDataCollector.ExternalData externalData,
                                      Sender sender) {
        this.context = context;
        this.externalData = externalData;
        this.externalData.appStartTime = System.currentTimeMillis();

        crashReportFinder = new CrashReportFinder(context);
        this.sender = sender;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ReportDataCollector collector = new ReportDataCollector();

        JSONObject jsonObject = collector.collect(context, ex, externalData);
        String filename = getReportFilename();

        CrashReportPersister crashReportPersister = new CrashReportPersister(context);

        // Try to send all report that failed to be send
        String[] files = crashReportFinder.getCrashReportFiles();
        String content;
        for (String fileStored : files) {
            content = crashReportPersister.load(fileStored);
            if (sender.send(content)) {
                context.deleteFile(fileStored);
            }
        }

        // Try to send the latest report and if failed save it
        if (!sender.send(jsonObject.toString())) {
            crashReportPersister.store(jsonObject, filename);
        }

        new ToastThread();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // ignore
        }

        Log.e("MyUncaught", ex.getMessage(), ex);

        android.os.Process.killProcess(Process.myPid());
        System.exit(10);
    }

    private String getReportFilename() {
        return String.valueOf(this.externalData.appStartTime) + REPORTFILE_EXTENSION;
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
