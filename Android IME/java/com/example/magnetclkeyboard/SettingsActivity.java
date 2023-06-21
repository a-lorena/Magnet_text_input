package com.example.magnetclkeyboard;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SettingsActivity extends PreferenceActivity implements SensorEventListener {

	private double deltaX = 0.0;
	private double deltaY = 0.0;
	private double deltaZ = 0.0;

	public static final String SHARED_PREFS = "sharedPrefsKeyboard";
	public static final String DELTA_X_VALUE = "deltaX";
	public static final String DELTA_Y_VALUE = "deltaY";
	public static final String DELTA_Z_VALUE = "deltaZ";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
		sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_FASTEST);

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		findPreference("magnetSet").setOnPreferenceClickListener(preference -> {
			// do something
			Toast.makeText(this, "deltaX: " + deltaX + "\ndeltaY: " + deltaY + "\ndeltaZ: " + deltaZ, Toast.LENGTH_SHORT).show();

			editor.putFloat(DELTA_X_VALUE, (float) deltaX);
			editor.putFloat(DELTA_Y_VALUE, (float) deltaY);
			editor.putFloat(DELTA_Z_VALUE, (float) deltaZ);
			editor.apply();

			return true;
		});

		findPreference("modelSet").setOnPreferenceClickListener(preference -> {
			Toast.makeText(this, "Treniranje modela za predviđanje u tijeku...", Toast.LENGTH_SHORT).show();

			Handler handler = new Handler();
			handler.postDelayed(() -> trainModel(), 1000);

			return true;
		});
	}

	private void trainModel() {
		AssetManager assetManager = getAssets();
		ArrayList<String> dataset = new ArrayList<>();

		for (int i = 1; i <= 15; i++) {
			dataset.add("User" + i + ".json");
		}

		for(String filename : dataset) {
			try {
				InputStream in = assetManager.open(filename);

				String outDir = getExternalFilesDir(null).getAbsolutePath();
				File outFile = new File(outDir, filename);
				OutputStream out = new FileOutputStream(outFile);

				copyFile(in, out);
				in.close();
				out.close();

			} catch(IOException e) {
				Log.e("tag", "Failed to copy asset file: " + filename, e);
			}
		}

		if (!Python.isStarted()) {
			Python.start(new AndroidPlatform(this.getApplicationContext()));
		}
		Python py = Python.getInstance();
		PyObject pyModule = py.getModule("TrainModel");

		String dirPath = getExternalFilesDir(null).getAbsolutePath();
		PyObject object = pyModule.callAttr("main", dirPath);
		Toast.makeText(this, object.toString(), Toast.LENGTH_LONG).show();
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		int sensorType = sensorEvent.sensor.getType();

		if (sensorType == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
			deltaX = sensorEvent.values[0];
			deltaY = sensorEvent.values[1];
			deltaZ = sensorEvent.values[2];
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}