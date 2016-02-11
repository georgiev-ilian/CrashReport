package crashreport.prototype.android.com.crashreport;

import android.app.Application;

/**
 * Created by Ilian Georgiev.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(this));
    }
}
