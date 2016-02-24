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

    // Keep the default handler in case something goes wrong with our handling
    private final Thread.UncaughtExceptionHandler defaultHandler;

    private final CrashReportPersister crashReportPersister;

    public MyUncaughtExceptionHandler(Context context,
                                      Thread.UncaughtExceptionHandler defaultHandler,
                                      ReportDataCollector.ExternalData externalData,
                                      Sender sender) {
        this.context = context;
        this.externalData = externalData;
        this.externalData.appStartTime = System.currentTimeMillis();

        crashReportFinder = new CrashReportFinder(context);
        this.sender = sender;

        this.defaultHandler = defaultHandler;
        this.crashReportPersister = new CrashReportPersister(context);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            ReportDataCollector collector = new ReportDataCollector();

            JSONObject jsonObject = collector.collect(context, ex, externalData);
            String filename = getReportFilename();

            // Save current crash report
            crashReportPersister.store(jsonObject, filename);

            new SendReportThread(filename, sender, jsonObject);

            new ToastThread();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // ignore
            }

            Log.e("MyUncaught", ex.getMessage(), ex);

            android.os.Process.killProcess(Process.myPid());
            System.exit(10);
        } catch (Throwable th) {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            }
        }
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

    private class SendReportThread implements Runnable {

        private final Sender sender;
        private final String filename;
        private final JSONObject jsonObject;

        public SendReportThread(String filename, Sender sender, JSONObject jsonObject) {
            this.sender = sender;
            this.filename = filename;
            this.jsonObject = jsonObject;

            new Thread(this).start();
        }

        @Override
        public void run() {
            // Try to send the latest report and if failed save it
            if (sender.send(jsonObject.toString())) {
                context.deleteFile(filename);

                // When this is ready send old reports
                new SendOldReportThread();
            }
        }
    }

    private class SendOldReportThread implements Runnable {

        public SendOldReportThread() {
            new Thread(this).start();
        }

        @Override
        public void run() {
            // Try to send all report that failed to be send
            String[] files = crashReportFinder.getCrashReportFiles();
            String content;
            for (String fileStored : files) {
                content = crashReportPersister.load(fileStored);
                if (sender.send(content)) {
                    context.deleteFile(fileStored);
                }
            }
        }
    }
}
