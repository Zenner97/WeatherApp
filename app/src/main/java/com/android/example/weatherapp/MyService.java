package com.android.example.weatherapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
//import android.app.AlertDialog.Builder;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.*;

public class MyService extends Service {
    SQLiteDatabase db = MainActivity.db;
    String url = MainActivity.url+7;
    Thread parsing;
    String pContent;
    Builder builder = new Builder(this);
    Notification notification;
    int i = 0;
    boolean shouldContinue = true;

    public void onCreate() {super.onCreate();}

    public void onDestroy() {
        shouldContinue = false;
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Service has stopped", Toast.LENGTH_SHORT).show();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        final int time = intent.getIntExtra("time", 0);
        Toast.makeText(getApplicationContext(), "Service has started", Toast.LENGTH_SHORT).show();
        i = 0;
        parsing = new Thread(new Runnable() {
            public void run() {
                while (shouldContinue)
                {
                    if (i == 7)
                    {
                        shouldContinue = false;
                        stopSelf();
                    }
                    else
                    {
                        getData();
                        if (pContent!=null) {
                            createNotification();
                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.notify(++i, notification);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        parsing.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void getData ()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject location = response.getJSONObject("location");
                        String city = location.getString("name");
                        JSONObject forecast = response.getJSONObject("forecast");
                        JSONArray forecastday = forecast.getJSONArray("forecastday");
                        JSONObject oneday = forecastday.getJSONObject(i);

                        String date = oneday.getString("date");
                        JSONObject day = oneday.getJSONObject("day");
                        double temp = day.getDouble("avgtemp_c");
                        double wind = day.getDouble("maxwind_kph");
                        double humidity = day.getDouble("avghumidity");
                        JSONObject condition = day.getJSONObject("condition");
                        String text = condition.getString("text");
                        if (date != null){
                            db.execSQL("INSERT INTO weather VALUES ('" + city + "','" + date +
                                    "'," + temp + "," + wind + "," + humidity + ",'" + text + " ');");
                            pContent = (date + " temp: " + temp);
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

    public void createNotification(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher, options);
        builder.setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Запись добавлена")
                .setContentText(pContent)
                .setContentIntent(resultPendingIntent);
        notification = builder.build();
    }
}
