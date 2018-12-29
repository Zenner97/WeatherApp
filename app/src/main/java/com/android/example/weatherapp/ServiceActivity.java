package com.android.example.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ServiceActivity extends Activity {

    SharedPreferences sp;
    EditText interval;
    final String SAVED_TEXT = "saved_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        interval = findViewById(R.id.parsingInterval);
        loadText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveText();
    }

    public void onClickStartService(View view)
    {
        String content = interval.getText().toString();
        try {
            int time = Integer.parseInt(content);
            if (time > 0) {
                startService(new Intent(this, MyService.class).putExtra("time", time));
            } else {
                Toast.makeText(getApplicationContext(), "Число должно быть больше 0", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Введите число", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickStopService(View view)
    {
        stopService(new Intent(this, MyService.class));
    }

    void saveText() {
        sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SAVED_TEXT, interval.getText().toString());
        editor.commit();
    }

    void loadText() {
        sp = getPreferences(MODE_PRIVATE);
        String savedText = sp.getString(SAVED_TEXT, "");
        interval.setText(savedText);
    }
}