package crashreport.prototype.android.com.crashreport;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String str = null;
        Log.d("MyApplication", "onCreate: " + str.toString());

    }
}
