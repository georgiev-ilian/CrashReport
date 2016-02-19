package my.crashreport;

import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Ilian Georgiev.
 */
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String REPORTFILE_EXTENSION = ".crashreport";

    public static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;

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

        store(jsonObject, filename);

        String[] files = crashReportFinder.getCrashReportFiles();
        String content;
        for (String fileStored :
                files) {
            content = load(fileStored);
            sender.send(fileStored, content, new Sender.Listener() {
                @Override
                public void onReportSend(String filename) {
                    Log.d("MyHandler", "onReportSend: " + filename);
                }
            });
        }

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
        OutputStreamWriter writer = null;
        try {
            out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(data.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }

                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                // ignore
            }
        }
    }

    private String load(String filename) {

        FileInputStream in = null;
        String result = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedReader bufferedReader = null;

        try {
            in = context.openFileInput(filename);

            if (in == null) {
                throw new IllegalArgumentException("Invalid crash report filename : " + filename);
            }

            bufferedInputStream = new BufferedInputStream(in, DEFAULT_BUFFER_SIZE_IN_BYTES);
            bufferedReader = new BufferedReader(
                    new InputStreamReader(bufferedInputStream, "UTF-8"));
            result = bufferedReader.readLine();

        } catch (IOException e) {
            // ignore
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (bufferedReader != null) {
                    bufferedReader.close();
                }

                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

        return result;
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

    private class SendThread implements Runnable {

        public SendThread() {
            new Thread(this).start();
        }

        @Override
        public void run() {
            String[] files = crashReportFinder.getCrashReportFiles();
        }
    }
}
