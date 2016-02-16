package my.crashreport;

import android.app.Application;

/**
 * Created by Ilian Georgiev.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(this));

        super.onCreate();
    }
}
