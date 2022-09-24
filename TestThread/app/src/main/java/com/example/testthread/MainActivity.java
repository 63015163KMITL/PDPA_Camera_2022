package com.example.testthread;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    String TAG = "Message";
    TextView txt ;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = findViewById(R.id.textView);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0 ;i<100;i++){
                    txt.setText(String.valueOf(i));
                }
            }
        });

        new Thread(new Runnable() {
            public void run() {
                // loop until the thread is interrupted
                while (true) {
                    Log.d(TAG, "run: ");;
                }
            }
        }).start();
    }

    private class SimpleTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "start task");
        }

        protected String doInBackground(String... urls)   {
            Log.d(TAG, "doInBackground: ");

            return "result";
        }

        protected void onPostExecute(String result)  {
            // Dismiss ProgressBar
            // updateWebView(result);
        }
    }

}

