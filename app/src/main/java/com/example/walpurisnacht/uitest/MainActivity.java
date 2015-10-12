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
import android.text.method.ScrollingMovementMethod;
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

    private float gx;
    private float gy;
    private float gz;

    TextView tmpViewer = null;
    TextView sttViewer = null;

    String header = null;
    String data = null;
    String abspath = null;

    String attrib = null;
    boolean Lock = false;

    int count = 0;

    public final static String EXTRA = "Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        abspath = "sdcard/csv/";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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
        File dir = new File(abspath);
        if (dir.exists() && dir.isDirectory()) {
            String[] fileList = dir.list();
            for (int i = 0; i < fileList.length; i++)   {
                new File(dir, fileList[i]).delete();
            }
        }
    }

    private void SaveData() {

        FileOutputStream outputStream;

        try {
            if (!isExternalStorageWritable()) return;

            //Save data
            File csvFolder = new File(abspath);
            csvFolder.mkdirs();

            File file = new File(csvFolder,Integer.toString(count) + ".csv");
            file.createNewFile();

            outputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

            String export = header + data;
            export = export.replace("null","");

            tmpViewer = (TextView) findViewById(R.id.Data);
            tmpViewer.setMovementMethod(new ScrollingMovementMethod());
            tmpViewer.setText(export);

            outputStreamWriter.write(export);
            outputStreamWriter.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //region "Function"
    public void Start_Click(View view) {

        //Safelock
        if (Lock) return;

        if ((attrib == "\"up\"") || (attrib == "\"down\"")) findViewById(R.id.udButton).performClick();
        else findViewById(R.id.lrButton).performClick();

        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        data = null;

        tmpViewer = (TextView) findViewById(R.id.Data);
        header = "\"label\",\"acc_x\",\"acc_y\",\"acc_z\".\"mag_x\",\"mag_y\",\"mag_z\",\"gyr_x\",\"gyr_y\",\"gyr_z\"" + "\n";
        //tmpViewer.setText(header);

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
                else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    gx = event.values[0];
                    gy = event.values[1];
                    gz = event.values[2];
                }

                if (Float.toString(ax) != null) {
                    data += attrib + "," + Float.toString(ax) + "," + Float.toString(ay) + "," + Float.toString(az) + ","
                            + Float.toString(mx) + "," + Float.toString(my) + "," + Float.toString(mz) + ","
                            + Float.toString(gx) + "," + Float.toString(gy) + "," + Float.toString(gz) + "\n";
                    //Accelerometer + Magnetic
                    //tmpViewer.setText(header + data);

                    tmpViewer.setText(header + "\n" + attrib + "," + Float.toString(ax) + "," + Float.toString(ay) + "," + Float.toString(az) + ","
                            + Float.toString(mx) + "," + Float.toString(my) + "," + Float.toString(mz)+ ","
                            + Float.toString(gx) + "," + Float.toString(gy) + "," + Float.toString(gz)  + "\n");
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);

        Lock = true;
    }

    public void Stop_Click(View view) {
        Lock = false;
        super.onStop();
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    public void Save_Click(View view) {
        SaveData();
        count++;
    }
    //endregion

    //region "Attribute select"
    public void UD_Click(View view) {
        if (attrib == "\"up\"") attrib = "\"down\"";
        else attrib = "\"up\"";
        sttViewer = (TextView) findViewById(R.id.Stat);
        sttViewer.setText(attrib);
    }

    public void LR_Click(View view) {
        if (attrib == "\"left\"") attrib = "\"right\"";
        else attrib = "\"left\"";
        sttViewer = (TextView) findViewById(R.id.Stat);
        sttViewer.setText(attrib);
    }
    //endregion
}
