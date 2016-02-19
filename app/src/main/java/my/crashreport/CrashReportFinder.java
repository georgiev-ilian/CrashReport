package my.crashreport;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Responsible for retrieving the location of crash report files.
 *
 * Created by Ilian Georgiev.
 */
final class CrashReportFinder {
    private final Context context;

    public CrashReportFinder(Context context) {
        this.context = context;
    }

    /**
     * @return Returns an array containing the names of crash report
     * files that are not sent.
     */
    public String[] getCrashReportFiles() {
        if (context == null) {
            return new String[0];
        }

        final File dir = context.getFilesDir();
        Log.d("CrashReportFinder", "getCrashReportFiles: " + dir);
        if (dir == null) {
            // Application files directory does not exist!
            return new String[0];
        }

        // Filter for files by name
        final FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(MyUncaughtExceptionHandler.REPORTFILE_EXTENSION);
            }
        };

        final String[] result = dir.list(filter);

        return (result == null) ? new String[0] : result;
    }
}
