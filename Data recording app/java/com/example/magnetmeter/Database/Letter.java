package com.example.magnetmeter.Database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.magnetmeter.Point;

import java.util.List;

@Entity(tableName = "letters")
public class Letter {
	@PrimaryKey(autoGenerate = true)
	private int id;

	private String letter;
	private String user;
	private double deltaX;
	private double deltaY;
	private double deltaZ;
	private long t0;
	private long tN;
	private double t;
	private Point pointList;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLetter() {
		return letter;
	}

	public void setLetter(String letter) {
		this.letter = letter;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public double getDeltaX() { return deltaX; }

	public void setDeltaX(double deltaX) {
		this.deltaX = deltaX;
	}

	public double getDeltaY() {
		return deltaY;
	}

	public void setDeltaY(double deltaY) {
		this.deltaY = deltaY;
	}

	public double getDeltaZ() {
		return deltaZ;
	}

	public void setDeltaZ(double deltaZ) {
		this.deltaZ = deltaZ;
	}

	public long getT0() {
		return t0;
	}

	public void setT0(long t0) {
		this.t0 = t0;
	}

	public long getTN() {
		return tN;
	}

	public void setTN(long tN) {
		this.tN = tN;
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
		this.t = t;
	}

	public Point getPointList() {
		return pointList;
	}

	public void setPointList(Point pointList) {
		this.pointList = pointList;
	}
}
