package com.example.magnetclkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class Keyboard extends InputMethodService implements SensorEventListener, View.OnClickListener {
	Button scriptButton;
	RelativeLayout controlsLayout, recordLayout, lettersLayout;

	Button writeButton, backspaceButton, spaceButton, enterButton;
	Button recordButton, cancelButton;
	Button top1, top2, top3, top4, backButton;
	InputConnection ic;

	private String layout;
	public static final String LAYOUT_CONTROLS = "controls";
	public static final String LAYOUT_RECORDING = "recording";
	public static final String LAYOUT_LETTERS = "letters";
	private Button pickedButton;

	boolean writing = false;
	boolean recording = false;
	Python py;
	PyObject pyModule;

	private double magX = 0.0;
	private double magY = 0.0;
	private double magZ = 0.0;
	private double deltaX = 0.0;
	private double deltaY = 0.0;
	private double deltaZ = 0.0;
	private List<Double> X;
	private List<Double> Y;
	private List<Double> Z;
	private List<Double> point;
	private List<List<Double>> fingerprint;

	SharedPreferences sharedPreferences;
	public static final String SHARED_PREFS = "sharedPrefsKeyboard";
	public static final String DELTA_X_VALUE = "deltaX";
	public static final String DELTA_Y_VALUE = "deltaY";
	public static final String DELTA_Z_VALUE = "deltaZ";

	@Override
	public View onCreateInputView() {
		View view = getLayoutInflater().inflate(R.layout.keyboard, null);

		// Prvi layout
		controlsLayout = view.findViewById(R.id.controlsLayout);
		scriptButton = view.findViewById(R.id.scriptButton);
		writeButton = view.findViewById(R.id.writeButton);
		enterButton = view.findViewById(R.id.enterButton);
		spaceButton = view.findViewById(R.id.spaceButton);
		backspaceButton = view.findViewById(R.id.backspaceButton);
		// Drugi layout
		recordLayout = view.findViewById(R.id.recordingLayout);
		recordButton = view.findViewById(R.id.recordButton);
		cancelButton = view.findViewById(R.id.cancelButton);
		// Treci layout
		lettersLayout = view.findViewById(R.id.lettersLayout);
		top1 = view.findViewById(R.id.top1);
		top2 = view.findViewById(R.id.top2);
		top3 = view.findViewById(R.id.top3);
		top4 = view.findViewById(R.id.top4);
		backButton = view.findViewById(R.id.backButton);

		layout = LAYOUT_CONTROLS;
		pickedButton = writeButton;
		setPickedColor();

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
		sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_FASTEST);

		sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		deltaX = sharedPreferences.getFloat(DELTA_X_VALUE, 0);
		deltaY = sharedPreferences.getFloat(DELTA_Y_VALUE, 0);
		deltaZ = sharedPreferences.getFloat(DELTA_Z_VALUE, 0);

		X = new ArrayList<>();
		Y = new ArrayList<>();
		Z = new ArrayList<>();
		fingerprint = new ArrayList<>();

		return view;
	}

	private void startScript() {
		if (!Python.isStarted()) {
			Python.start(new AndroidPlatform(this.getApplicationContext()));
		}
		py = Python.getInstance();
		pyModule = py.getModule("Predict");

		String dirPath = getExternalFilesDir(null).getAbsolutePath();
		PyObject object = pyModule.callAttr("start", dirPath);

		Toast.makeText(this, object.toString(), Toast.LENGTH_SHORT).show();
	}

	private void clickedScript() {
		Toast.makeText(this, "Učitavanje skripe u tijeku...", Toast.LENGTH_SHORT).show();

		Handler handler = new Handler();
		handler.postDelayed(() -> startScript(), 1000);
	}

	private void clickedWrite() {
		if (pyModule == null) {
			Toast.makeText(this, "Prije pisanja učitajte skriptu!", Toast.LENGTH_SHORT).show();
		} else {
			writing = true;
			layout = LAYOUT_RECORDING;
			controlsLayout.setVisibility(View.INVISIBLE);
			recordLayout.setVisibility(View.VISIBLE);
			setNormalColor();
			pickedButton = recordButton;
			setPickedColor();
		}
	}

	private void clickedRecord() {
		recording = true;
		cancelButton.setVisibility(View.INVISIBLE);
		recordButton.setText(R.string.stop);
	}

	private void clickedStop() {
		recording = false;
		layout = LAYOUT_LETTERS;

		predictLetter(X, Y, Z, fingerprint);

		X.clear();
		Y.clear();
		Z.clear();
		fingerprint.clear();
	}

	private void clickedCancel() {
		writing = false;
		layout = LAYOUT_CONTROLS;
		recordLayout.setVisibility(View.INVISIBLE);
		controlsLayout.setVisibility(View.VISIBLE);
		setNormalColor();
		pickedButton = writeButton;
		setPickedColor();
	}

	private void predictLetter(List<Double> X, List<Double> Y, List<Double> Z, List<List<Double>> fingerprint) {
		String dirPath = getExternalFilesDir(null).getAbsolutePath();

		Double[] arrayX = X.toArray(new Double[0]);
		Double[] arrayY = Y.toArray(new Double[0]);
		Double[] arrayZ = Z.toArray(new Double[0]);

		Double[][] arr = new Double[fingerprint.size()][];
		int i = 0;
		for (List<Double> l: fingerprint) {
			arr[i++] = l.toArray(new Double[3]);
		}

		PyObject object = pyModule.callAttr("main", arrayX, arrayY, arrayZ, arr, dirPath);

		String[] result = object.toJava(String[].class);

		top1.setText(String.valueOf(result[0]));
		top2.setText(String.valueOf(result[1]));
		top3.setText(String.valueOf(result[2]));
		top4.setText(String.valueOf(result[3]));

		setNormalColor();
		pickedButton = top1;
		setPickedColor();

		cancelButton.setVisibility(View.VISIBLE);
		recordLayout.setVisibility(View.INVISIBLE);
		recordButton.setText(R.string.record);
		lettersLayout.setVisibility(View.VISIBLE);
	}

	private void clickedBack() {
		layout = LAYOUT_RECORDING;
		lettersLayout.setVisibility(View.INVISIBLE);
		recordLayout.setVisibility(View.VISIBLE);
		setNormalColor();
		pickedButton = recordButton;
		setPickedColor();
	}

	private void writeLetter(String letter) {
		ic.commitText(letter, 1);
		setNormalColor();
		pickedButton = recordButton;
		setPickedColor();
		lettersLayout.setVisibility(View.INVISIBLE);
		recordLayout.setVisibility(View.VISIBLE);
		layout = LAYOUT_RECORDING;
	}

	private void setPickedColor() {
		if (pickedButton == writeButton || pickedButton == recordButton) {
			pickedButton.setBackgroundResource(R.drawable.picked_record);
		} else {
			pickedButton.setBackgroundResource(R.drawable.picked);
		}
		pickedButton.setTextColor(getResources().getColor(R.color.black));
		pickedButton.setTag(R.drawable.picked);
	}

	private void setNormalColor() {
		if (pickedButton == writeButton || pickedButton == recordButton) {
			pickedButton.setBackgroundResource(R.drawable.normal_record);
		} else {
			pickedButton.setBackgroundResource(R.drawable.normal);
		}
		pickedButton.setTextColor(getResources().getColor(R.color.white));
		pickedButton.setTag(R.drawable.normal);
	}

	private void goUp() {
		setNormalColor();
		switch (layout) {
			case LAYOUT_CONTROLS:
				pickedButton = scriptButton;
				break;

			case LAYOUT_RECORDING:
				pickedButton = cancelButton;
				break;

			case LAYOUT_LETTERS:
				pickedButton = backButton;
				break;
		}
		setPickedColor();
	}

	private void goDown() {
		setNormalColor();
		switch (layout) {
			case LAYOUT_CONTROLS:
				pickedButton = spaceButton;
				break;

			case LAYOUT_RECORDING:
				pickedButton = recordButton;
				break;

			case LAYOUT_LETTERS:
				pickedButton = top3;
				break;
		}
		setPickedColor();
	}

	private void goCenter() {
		setNormalColor();
		switch (layout) {
			case LAYOUT_CONTROLS:
				pickedButton = writeButton;
				break;

			case LAYOUT_RECORDING:
				pickedButton = recordButton;
				break;

			case LAYOUT_LETTERS:
				pickedButton = top1;
				break;
		}
		setPickedColor();
	}

	private void goLeft() {
		setNormalColor();
		switch (layout) {
			case LAYOUT_CONTROLS:
				pickedButton = enterButton;
				break;

			case LAYOUT_RECORDING:
				pickedButton = recordButton;
				break;

			case LAYOUT_LETTERS:
				pickedButton = top4;
				break;
		}
		setPickedColor();
	}

	private void goRight() {
		setNormalColor();
		switch (layout) {
			case LAYOUT_CONTROLS:
				pickedButton = backspaceButton;
				break;

			case LAYOUT_RECORDING:
				pickedButton = recordButton;
				break;

			case LAYOUT_LETTERS:
				pickedButton = top2;
		}
		setPickedColor();
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		int sensorType = sensorEvent.sensor.getType();

		if (sensorType == Sensor.TYPE_PROXIMITY) {
			float cm = sensorEvent.values[0];
			ic = getCurrentInputConnection();

			if (cm < 1) {
				int pickedID = pickedButton.getId();

				if (layout.equals(LAYOUT_CONTROLS)) {
					switch (pickedID) {
						case R.id.scriptButton:
							clickedScript();
							break;

						case R.id.writeButton:
							clickedWrite();
							break;

						case R.id.backspaceButton:
							ic.deleteSurroundingText(1, 0);
							break;

						case R.id.spaceButton:
							ic.commitText(" ", 1);
							break;

						case R.id.enterButton:
							ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
							break;
					}
				} else if (layout.equals(LAYOUT_RECORDING)) {
					switch (pickedID) {
						case R.id.recordButton:
							if (recording) {
								clickedStop();
							} else {
								clickedRecord();
							}
							break;

						case R.id.cancelButton:
							clickedCancel();
							break;
					}
				} else {
					if (pickedID == R.id.backButton) {
						clickedBack();
					} else {
						String letter = pickedButton.getText().toString();
						writeLetter(letter.toLowerCase());
					}
				}
			}
		} else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
			magX = sensorEvent.values[0];
			magY = sensorEvent.values[1];
			magZ = sensorEvent.values[2];

			magX -= deltaX;
			magY -= deltaY;
			magZ -= deltaZ;

			if (recording) {
				point = new ArrayList<>();

				point.add(magX);
				point.add(magY);
				point.add(magZ);

				fingerprint.add(point);

				X.add(magX);
				Y.add(magY);
				Z.add(magZ);
			} else {
				if (magX > 0 && magY > 0 & magZ < 0) {
					goLeft();
				} else if (magX < 0 && magY < 0 && magZ > 0) {
					goRight();
				} else if (magX < 0 && magY < 0 && magZ < 0) {
					goUp();
				} else if (magX < 0 && magY > 0 && magZ < 0) {
					if (magX < -100 && magY > 100 && magZ < -100) {
						goCenter();
					} else if (magX > -100 && magY < 100 && magZ > -100) {
						goDown();
					}
				}
			}

		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	@Override
	public void onClick(View view) {

	}
}
