package com.example.magnetmeter.Database;

import androidx.room.TypeConverter;

import com.example.magnetmeter.Point;
import com.google.gson.Gson;

public class Converters {
	@TypeConverter
	public static String fromPoint(Point h) {
		return new Gson().toJson(h);
	}
	@TypeConverter
	public static Point toPoint(String s) {
		return new Gson().fromJson(s, Point.class);
	}
}
