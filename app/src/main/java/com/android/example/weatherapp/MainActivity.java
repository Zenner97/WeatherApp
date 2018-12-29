package com.android.example.weatherapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private TextView parseDataText;
    private Button parseData;
    private Button showDB;
    private Button ShowNot;
    private Intent intent;
    public static SQLiteDatabase db;
    public static String url = "https://api.apixu.com/v1/forecast.json?key=3c748e8dcfbc4e009b081536182912&q=Paris&days="+3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parseDataText = findViewById(R.id.parseDataText);
        parseData = findViewById(R.id.parseData);
        showDB = findViewById(R.id.showData);
        ShowNot = findViewById(R.id.showNot);
        requestQueue = Volley.newRequestQueue(this);

        db = getBaseContext().openOrCreateDatabase("weather.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS weather (city TEXT, date TEXT, temperature REAL, wind REAL, humidity REAL, condition TEXT)");

        parseData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                parseDataText.setText("");
                                JSONObject location = response.getJSONObject("location");
                                String city = location.getString("name");

                                parseDataText.append("Weather in " + city + "\n\n");
                                JSONObject forecast = response.getJSONObject("forecast");
                                JSONArray forecastday = forecast.getJSONArray("forecastday");

                                for (int i = 0; i < forecastday.length(); i++) {
                                    JSONObject oneday = forecastday.getJSONObject(i);

                                    String date = oneday.getString("date");
                                    JSONObject day = oneday.getJSONObject("day");
                                    double temp = day.getDouble("avgtemp_c");
                                    double wind = day.getDouble("maxwind_kph");
                                    double humidity = day.getDouble("avghumidity");
                                    JSONObject condition = day.getJSONObject("condition");
                                    String text = condition.getString("text");

                                    parseDataText.append("Date: " + date + "\n");
                                    parseDataText.append("Temperature: " + temp + "\n");
                                    parseDataText.append("Wind speed: " + wind + "\n");
                                    parseDataText.append("Humidity: " + humidity + "\n");
                                    parseDataText.append("Condition: " + text + "\n\n");

                                    db.execSQL("INSERT INTO weather VALUES ('" + city + "','" + date + "'," + temp + "," + wind + "," + humidity + ",'" + text + "');");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                requestQueue.add(request);
            }
        });

        showDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseDataText.setText("");
                Cursor query = db.rawQuery("SELECT * FROM weather", null);
                int querySize = query.getCount() - 3;
                query = db.rawQuery("SELECT * FROM weather LIMIT "+ querySize +", 3", null);
                if(query.moveToFirst()){
                    do
                    {
                        String city = query.getString(0);
                        String date = query.getString(1);
                        double temp = query.getDouble(2);
                        double wind = query.getDouble(3);
                        double humidity = query.getDouble(4);
                        String text = query.getString(5);
                        parseDataText.append("City: " + city + "\n");
                        parseDataText.append("Date: " + date + "\n");
                        parseDataText.append("Temperature: " + temp + "\n");
                        parseDataText.append("Wind speed: " + wind + "\n");
                        parseDataText.append("Humidity: " + humidity + "\n");
                        parseDataText.append("Condition: " + text + "\n\n");
                    }
                    while(query.moveToNext());
                }
                query.close();
            }
        });

        //db.close();
    }

    public void onClickShowNote(View view) {
        Intent intent = new Intent(this, ServiceActivity.class);
        startActivity(intent);
    }
}
