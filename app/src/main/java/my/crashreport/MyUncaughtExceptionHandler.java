package my.crashreport;

import android.content.Context;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Ilian Georgiev.
 */
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String REPORTFILE_EXTENSION = ".crashreport";

    private final Context context;

    private final ReportDataCollector.ExternalData externalData;

    public MyUncaughtExceptionHandler(Context context,
                                      ReportDataCollector.ExternalData externalData) {
        this.context = context;
        this.externalData = externalData;
        this.externalData.appStartTime = System.currentTimeMillis();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ReportDataCollector collector = new ReportDataCollector();

        JSONObject jsonObject = collector.collect(context, ex, externalData);
        String filename = getReportFilename();

        store(jsonObject, filename);

        Log.d("MyHandler", jsonObject.toString());

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

    private void store(JSONObject data, String fileName) {

        OutputStream out = null;
        try {
            out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(data.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
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
}
