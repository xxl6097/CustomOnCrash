package uuxia.com.sample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Button crashMainThreadButton = (Button) findViewById(R.id.button_crash_main_thread);
        Button crashBgThreadButton = (Button) findViewById(R.id.button_crash_bg_thread);
        Button crashWithDelayButton = (Button) findViewById(R.id.button_crash_with_delay);

        crashMainThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                throw new RuntimeException("I'm a cool exception and I crashed the main thread!");
            }
        });

        crashBgThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        throw new RuntimeException("I'm also cool, and I crashed the background thread!");
                    }
                }.execute();
            }
        });

        crashWithDelayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            //meh
                        }
                        throw new RuntimeException("I am a not so cool exception, and I am delayed, so you can check if the app crashes when in background!)");
                    }
                }.execute();
            }
        });
    }
}
