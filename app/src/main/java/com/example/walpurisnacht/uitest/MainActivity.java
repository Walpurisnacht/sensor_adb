package com.example.walpurisnacht.uitest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    private float ax;
    private float ay;
    private float az;

    private float mx;
    private float my;
    private float mz;

    private

    TextView tmpViewer = null;
    String tmp = null;
    String path = null;

    int count = 0;

    public final static String EXTRA = "Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId())   {
            case R.id.action_settings:
                //TODO clean
                DeleteData();
                tmpViewer = (TextView) findViewById(R.id.Data);
                tmpViewer.setText("Folder cleared!");
                count = 0;
                break;
            case R.id.quit:
                this.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                super.onDestroy();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    private void DeleteData() {
        if (!isExternalStorageWritable()) return;
        File dir = new File("/sdcard/csv");
        if (dir.exists() && dir.isDirectory()) {
            String[] fileList = dir.list();
            for (int i = 0; i < fileList.length; i++)   {
                new File(dir, fileList[i]).delete();
            }
        }
    }

    private void SaveData() {

        path = Integer.toString(count) + ".csv";

        FileOutputStream outputStream;

        try {
            if (!isExternalStorageWritable()) return;

            //Fetch data
            tmpViewer = (TextView) findViewById(R.id.Data);
            tmp = tmpViewer.getText().toString();

            //Save data
            File csvFolder = new File("/sdcard/csv");
            csvFolder.mkdirs();

            File file = new File(csvFolder,path);
            file.createNewFile();

            outputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

            outputStreamWriter.write(tmp);
            outputStreamWriter.close();
            outputStream.close();

            Toast.makeText(getBaseContext(), "Saved: " + path, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Start_Click(View view) {
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        tmpViewer = (TextView) findViewById(R.id.Data);
        tmp = "\"acc_x\",\"acc_y\",\"acc_z\".\"mag_x\",\"mag_y\",\"mag_z\"\n";
        tmpViewer.setText(tmp);

        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;

                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)  {
                    ax = event.values[0];
                    ay = event.values[1];
                    az = event.values[2];
                }
                else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)    {
                    mx = event.values[0];
                    my = event.values[1];
                    mz = event.values[2];
                }

                tmp += new String( Float.toString(ax) + "," + Float.toString(ay) + "," + Float.toString(az) + ","
                                + Float.toString(mx) + "," + Float.toString(my) + "," + Float.toString(mz) + "\n");
                //Accelerometer + Magnetic
                tmpViewer.setText(tmp);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);

        Toast.makeText(getBaseContext(),"Started",Toast.LENGTH_SHORT).show();
    }

    public void Stop_Click(View view) {
        super.onStop();
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));

        Toast.makeText(getBaseContext(),"Stopped",Toast.LENGTH_SHORT).show();
    }

    public void Save_Click(View view) {
        SaveData();
        count++;
    }
}
