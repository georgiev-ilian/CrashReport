package my.crashreport;

import android.content.Context;

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
public class CrashReportPersister {

    private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;

    private final Context context;

    public CrashReportPersister(Context context) {
        this.context = context;
    }

    public void store(JSONObject data, String fileName) {

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

    public String load(String filename) {

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
}
