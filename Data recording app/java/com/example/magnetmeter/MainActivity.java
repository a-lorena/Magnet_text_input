package com.example.magnetmeter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.magnetmeter.Database.AppDatabase;
import com.example.magnetmeter.Database.Letter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
	SensorManager sensorManager;
	Sensor sensor;
	boolean registered = false;

	private int repeatNumber = 15;
	private double deltaX = 0.0;
	private double deltaY = 0.0;
	private double deltaZ = 0.0;
	private double magX = 0.0;
	private double magY = 0.0;
	private double magZ = 0.0;
	private long timeStart;
	private long timeEnd;
	private double time;
	private List<Double> X;
	private List<Double> Y;
	private List<Double> Z;

 	SharedPreferences sharedPreferences;
	public static final String SHARED_PREFS = "sharedPrefsDataApp";
	public static final String DELTA_X_VALUE = "deltaX";
	public static final String DELTA_Y_VALUE = "deltaY";
	public static final String DELTA_Z_VALUE = "deltaZ";
	public static final String REPEAT_NUMBER = "repeatNumber";

	Character[] letters =  {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
			'U', 'V', 'W', 'X', 'Y', 'Z'};
	ArrayList<Character> arrayList = new ArrayList<>();
	Character currentLetter;
	int count = 0;
	int writtenLetters = 0;
	boolean writingDone = false;

	Toolbar toolbar;
	TextView xValue, yValue, zValue, letterCounter, letterMax, letter;
	EditText userInput;
	Button resetButton, startButton, stopButton;
	AppDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		xValue = findViewById(R.id.x_value);
		yValue = findViewById(R.id.y_value);
		zValue = findViewById(R.id.z_value);
		userInput = findViewById(R.id.user_input);
		letterCounter = findViewById(R.id.letter_counter);
		letterMax = findViewById(R.id.letter_max);
		letter = findViewById(R.id.letter_value);
		resetButton = findViewById(R.id.reset_button);
		startButton = findViewById(R.id.start_button);
		stopButton = findViewById(R.id.stop_button);

		db = AppDatabase.getInstance(this);
		X = new ArrayList<>();
		Y = new ArrayList<>();
		Z = new ArrayList<>();

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

		sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		deltaX = sharedPreferences.getFloat(DELTA_X_VALUE, 0);
		deltaY = sharedPreferences.getFloat(DELTA_Y_VALUE, 0);
		deltaZ = sharedPreferences.getFloat(DELTA_Z_VALUE, 0);
		repeatNumber = sharedPreferences.getInt(REPEAT_NUMBER, 15);

		int maxCounter = repeatNumber * 26;
		String newText = "/" + maxCounter + " (" + repeatNumber + ")";
		letterMax.setText(newText);

		resetButton.setOnClickListener(this);
		startButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		int sensorType = sensorEvent.sensor.getType();

		if (sensorType == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
			deltaX = sharedPreferences.getFloat(DELTA_X_VALUE, 0);
			deltaY = sharedPreferences.getFloat(DELTA_Y_VALUE, 0);
			deltaZ = sharedPreferences.getFloat(DELTA_Z_VALUE, 0);

			magX = sensorEvent.values[0];
			magY = sensorEvent.values[1];
			magZ = sensorEvent.values[2];

			magX -= deltaX;
			magY -= deltaY;
			magZ -= deltaZ;

			if (registered) {
				X.add(magX);
				Y.add(magY);
				Z.add(magZ);

				xValue.setText(String.valueOf(magX));
				yValue.setText(String.valueOf(magY));
				zValue.setText(String.valueOf(magZ));
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.reset_button:
				repeatNumber = sharedPreferences.getInt(REPEAT_NUMBER, 15);
				int maxCounter = repeatNumber * 26;
				String newText = "/" + maxCounter + " (" + repeatNumber + ")";
				letterMax.setText(newText);

				startButton.setVisibility(View.VISIBLE);
				stopButton.setVisibility(View.VISIBLE);

				writingDone = false;
				count = 0;
				writtenLetters = 0;
				arrayList.clear();

				arrayList.addAll(Arrays.asList(letters));
				Collections.shuffle(arrayList);
				currentLetter = arrayList.get(0);

				letterCounter.setText(String.valueOf(writtenLetters));
				letter.setText(String.valueOf(currentLetter));
				break;

			case R.id.start_button:
				startButton.setBackgroundResource(R.drawable.light_green_start);
				registered = true;
				timeStart = System.currentTimeMillis();
				break;

			case R.id.stop_button:
				startButton.setBackgroundResource(R.drawable.round_button_start);
				registered = false;
				timeEnd = System.currentTimeMillis();
				time = (timeEnd - timeStart) / 1000.00;

				String letter = String.valueOf(currentLetter);
				String user = userInput.getText().toString();

				saveToDatabase(letter, user);
				X.clear();
				Y.clear();
				Z.clear();
				xValue.setText("");
				yValue.setText("");
				zValue.setText("");
				getLetter();
				break;
		}
	}

	private void saveToDatabase(String letter, String user) {
		Letter l = new Letter();
		Point p = new Point();
		p.point = new ArrayList<>();
		p.point.add(X);
		p.point.add(Y);
		p.point.add(Z);

		l.setLetter(letter);
		l.setUser(user);
		l.setDeltaX(deltaX);
		l.setDeltaY(deltaY);
		l.setDeltaZ(deltaZ);
		l.setT0(timeStart);
		l.setTN(timeEnd);
		l.setT(time);
		l.setPointList(p);

		db.letterDAO().insertLetter(l);
	}

	private void getLetter() {
		writtenLetters += 1;
		letterCounter.setText(String.valueOf(writtenLetters));
		arrayList.remove(0);

		if (arrayList.isEmpty()) {
			count += 1;
			if (count == repeatNumber) {
				startButton.setVisibility(View.INVISIBLE);
				stopButton.setVisibility(View.INVISIBLE);
				letter.setText("");
				writingDone = true;
			} else {
				arrayList.addAll(Arrays.asList(letters));
				Collections.shuffle(arrayList);
			}
		}

		if (!writingDone) {
			currentLetter = arrayList.get(0);
			letter.setText(String.valueOf(currentLetter));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save_data:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}