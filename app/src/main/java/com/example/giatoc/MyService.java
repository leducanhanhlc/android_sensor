package com.example.giatoc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class MyService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "Leonardo";
    private static final String ACTION_STOP_SERVICE = "STOP";

    private SensorManager sensorManager;
    private Sensor sensor;
    private Integer actionCount;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Toast.makeText(this.getApplicationContext(), "onCreate", Toast.LENGTH_LONG).show();
        super.onCreate();
        createNotificationChannel();
        startServiceWithNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.ACTION_STOP_SERVICE.equals(intent.getAction()))
            stopMyService();
        actionCount = 0;
        Toast.makeText(this.getApplicationContext(), "onStartCommand", Toast.LENGTH_LONG).show();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    // In case the service is deleted or crashes some how
    @Override
    public void onDestroy() {
        actionCount = 0;
        Toast.makeText(this.getApplicationContext(), "onDestroy", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        Toast.makeText(this.getApplicationContext(), "onBind", Toast.LENGTH_LONG).show();
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this.getApplicationContext(), "onUnbind", Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }

    void startServiceWithNotification() {
        Toast.makeText(this.getApplicationContext(), "Notification", Toast.LENGTH_LONG).show();

        Intent NotificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =  PendingIntent.getActivity(this, 0, NotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String textTitle = "Title";
        String textContent = "Running";

        Intent stopSelf = new Intent(this, MyService.class);
        stopSelf.setAction(this.ACTION_STOP_SERVICE);

        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_background, "Stop", pStopSelf);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(123, builder.build());
        startForeground(123, builder.build());

    }

    void stopMyService() {
        stopForeground(true);
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = (float) 0.8;

        // Isolate the force of gravity with the low-pass filter.
        float[] gravity = new float[3];
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        float[] linear_acceleration = new float[3];
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        for (int i = 0; i < 3; i++) {
            if (linear_acceleration[i] > 20) {
                //actionCount++;
                //Toast.makeText(this.getApplicationContext(), String.valueOf(actionCount), Toast.LENGTH_LONG).show();
                //if (actionCount % 3 == 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(takePictureIntent);
                //}
                break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}