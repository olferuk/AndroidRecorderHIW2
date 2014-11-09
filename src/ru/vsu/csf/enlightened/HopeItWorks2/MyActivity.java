package ru.vsu.csf.enlightened.HopeItWorks2;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.*;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MyActivity extends Activity {

    Button startRecording;
    Button stopRecording;
    Button clearDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        startRecording = (Button) findViewById(R.id.start);
        stopRecording = (Button) findViewById(R.id.stop);
        clearDB = (Button) findViewById(R.id.clear);

        stopRecording.setEnabled(false);
    }

    public void startRecording(View view) {
        startRecording.setEnabled(false);
        stopRecording.setEnabled(true);

        Intent intent = new Intent(this, RecordService.class);
        startService(intent);
    }

    public void stopRecording(View view) {
        startRecording.setEnabled(true);
        stopRecording.setEnabled(false);

        stopService(new Intent(MyActivity.this, RecordService.class));
    }

    public void clearDB(View view) {
        SQLiteDatabase db = (new DBHelper(MyActivity.this)).getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_DATA_NAME + " ;");
        db.close();

        Toast.makeText(this, "Cleared!", Toast.LENGTH_SHORT).show();
    }
}
