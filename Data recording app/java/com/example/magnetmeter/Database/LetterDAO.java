package com.example.magnetmeter.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LetterDAO {
	@Insert
	void insertLetter(Letter letter);

	@Query("SELECT * FROM letters")
	List<Letter> getAll();

	@Query("SELECT * FROM letters WHERE letter = :letter")
	List<Letter> getByLetter(String letter);

	@Query("SELECT * FROM letters WHERE user = :user")
	List<Letter> getByUser(String user);

	@Query("SELECT * FROM letters WHERE letter = :letter AND user = :user")
	List<Letter> getData(String letter, String user);

	@Query("SELECT pointList FROM letters")
	String getPointsList();
}
