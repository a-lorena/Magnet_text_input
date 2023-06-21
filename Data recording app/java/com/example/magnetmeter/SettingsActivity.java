package com.example.magnetmeter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.JsonWriter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.magnetmeter.Database.AppDatabase;
import com.example.magnetmeter.Database.Letter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SensorEventListener {
	EditText repeatNumberInput, fileInput, letterInput, userInput;
	Button calibrateButton, saveRepeatNumber, saveFiltered, saveAll;
	AppDatabase db;

	private double deltaX = 0.0;
	private double deltaY = 0.0;
	private double deltaZ = 0.0;
	private int repeatNumber = 15;
	public static final String SHARED_PREFS = "sharedPrefsDataApp";
	public static final String DELTA_X_VALUE = "deltaX";
	public static final String DELTA_Y_VALUE = "deltaY";
	public static final String DELTA_Z_VALUE = "deltaZ";
	public static final String REPEAT_NUMBER = "repeatNumber";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		repeatNumberInput = findViewById(R.id.repetition_input);
		fileInput = findViewById(R.id.file_name_input);
		letterInput = findViewById(R.id.letter_filter_input);
		userInput = findViewById(R.id.user_filter_input);
		saveRepeatNumber = findViewById(R.id.repetition_save_button);
		calibrateButton = findViewById(R.id.calibrate_button);
		saveFiltered = findViewById(R.id.save_filtered);
		saveAll = findViewById(R.id.save_all_button);

		db = AppDatabase.getInstance(this);

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
		sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_FASTEST);

		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		repeatNumber = sharedPreferences.getInt(REPEAT_NUMBER, 15);

		repeatNumberInput.setText(String.valueOf(repeatNumber));

		saveRepeatNumber.setOnClickListener(view -> {
			String text = repeatNumberInput.getText().toString();
			repeatNumber = Integer.parseInt(text);

			editor.putInt(REPEAT_NUMBER, repeatNumber);
			editor.apply();

			Toast.makeText(this, "Number of repetitions set to " + repeatNumber + ".", Toast.LENGTH_SHORT).show();
		});

		calibrateButton.setOnClickListener(view -> {
			Toast.makeText(SettingsActivity.this, "deltaX: " + deltaX + "\ndeltaY: " + deltaY + "\ndeltaZ: " + deltaZ, Toast.LENGTH_SHORT).show();

			editor.putFloat(DELTA_X_VALUE, (float) deltaX);
			editor.putFloat(DELTA_Y_VALUE, (float) deltaY);
			editor.putFloat(DELTA_Z_VALUE, (float) deltaZ);
			editor.apply();
		});

		saveFiltered.setOnClickListener(view -> {
			String fileName = fileInput.getText().toString();
			String letter = letterInput.getText().toString();
			String user = userInput.getText().toString();

			String JSON_NAME = fileName + ".json";
			List<Letter> list;

			if (fileName.isEmpty()) {
				Toast.makeText(this, "Enter the file name.", Toast.LENGTH_SHORT).show();
			} else {
				if (letter.isEmpty() && user.isEmpty()) {
					Toast.makeText(this, "Enter a letter and/or user.", Toast.LENGTH_SHORT).show();
				} else {
					if (letter.isEmpty()) {
						list = db.letterDAO().getByUser(user);
					} else if (user.isEmpty()) {
						list = db.letterDAO().getByLetter(letter);
					} else {
						list = db.letterDAO().getData(letter, user);
					}

					try {
						saveJSON(JSON_NAME, list);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		saveAll.setOnClickListener(view -> {
			String fileName = fileInput.getText().toString();

			String JSON_NAME = fileName + ".json";
			List<Letter> list;

			if (fileName.isEmpty()) {
				Toast.makeText(this, "Enter the file name.", Toast.LENGTH_SHORT).show();
			} else {
				list = db.letterDAO().getAll();

				try {
					saveJSON(JSON_NAME, list);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void saveJSON(String JSON_NAME, List<Letter> list) throws Exception {
		File dir = getExternalFilesDir(null);
		File file = new File(dir, JSON_NAME);
		OutputStream fos = new FileOutputStream(file);
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));

		writer.setIndent("  ");
		writer.beginArray();

		for (int i = 0; i < list.size(); i++) {
			Point pointList = list.get(i).getPointList();

			writer.beginObject();
			writer.name("letter").value(list.get(i).getLetter());
			writer.name("user").value(list.get(i).getUser());
			writer.name("calibration");
			writer.beginObject();
			writer.name("deltaX").value(list.get(i).getDeltaX());
			writer.name("deltaY").value(list.get(i).getDeltaY());
			writer.name("deltaZ").value(list.get(i).getDeltaZ());
			writer.endObject();
			writer.name("t0").value(list.get(i).getT0());
			writer.name("tN").value(list.get(i).getTN());
			writer.name("t").value(list.get(i).getT());
			writer.name("fingerprint");
			writer.beginArray();
			for (int n = 0; n < pointList.point.get(0).size(); n++) {
				writer.beginArray();
				for (int m = 0; m < 3; m++) {
					writer.value(pointList.point.get(m).get(n));
				}
				writer.endArray();
			}
			writer.endArray();
			writer.endObject();
		}

		writer.endArray();
		writer.close();
		fos.close();
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