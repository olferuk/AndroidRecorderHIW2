package ru.vsu.csf.enlightened.HopeItWorks2;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RecordService extends Service implements SensorEventListener {

    DBHelper dbHelper;
    SQLiteDatabase db;

    Thread taskThread;

    SensorManager sensorManager;

    int orientationSensor;
    float headingAngle;
    float pitchAngle;
    float rollAngle;

    int accelerometerSensor;
    float xAxis;
    float yAxis;
    float zAxis;

    static int id = 1;

    ArrayList<float[]> history;

    public RecordService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = new DBHelper(RecordService.this);
        db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationSensor = Sensor.TYPE_ORIENTATION;
        accelerometerSensor = Sensor.TYPE_ACCELEROMETER;

        history = new ArrayList<float[]>();

        taskThread = new Thread(new Runnable() {
            @Override
            public void run() {

                sensorManager.registerListener(RecordService.this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
                sensorManager.registerListener(RecordService.this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
                sensorManager.registerListener(RecordService.this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);

                boolean finished = false;

                do {
                    history.add(new float[]{headingAngle, pitchAngle, rollAngle, xAxis, yAxis, zAxis});

                    /*if (history.size() == 100) {
                        ContentValues cv = new ContentValues();

                        db = dbHelper.getWritableDatabase();

                        for (float[] record : history) {
                            cv.put("record_id", id);

                            cv.put("heading", record[0]);
                            cv.put("pitch", record[1]);
                            cv.put("roll", record[2]);

                            cv.put("x", record[3]);
                            cv.put("y", record[4]);
                            cv.put("z", record[5]);

                            db.insert(DBHelper.TABLE_DATA_NAME, null, cv);
                        }

                        history.clear();
                        dbHelper.close();
                    }*/


                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    }
                    catch (InterruptedException e) {
                        finished = true;

                        ContentValues cv = new ContentValues();
                        db = dbHelper.getWritableDatabase();

                        for (float[] record : history) {
                            cv.put("record_id", id);

                            cv.put("heading", record[0]);
                            cv.put("pitch", record[1]);
                            cv.put("roll", record[2]);

                            cv.put("x", record[3]);
                            cv.put("y", record[4]);
                            cv.put("z", record[5]);

                            db.insert(DBHelper.TABLE_DATA_NAME, null, cv);
                        }

                        history.clear();
                        dbHelper.close();
                    }
                } while (!finished);
            }
        });
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
                headingAngle = event.values[0];
                pitchAngle = event.values[1];
                rollAngle = event.values[2];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                xAxis = event.values[0];
                yAxis = event.values[1];
                zAxis = event.values[2];
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        record();
        return super.onStartCommand(intent, flags, startId);
    }

    private void record() {
        taskThread.start();
        id++;
    }

    @Override
    public void onDestroy() {
        taskThread.interrupt();

        sensorManager.unregisterListener(RecordService.this);
        super.onDestroy();
    }
}
