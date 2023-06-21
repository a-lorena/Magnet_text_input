package com.example.magnetmeter.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Letter.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
	public abstract LetterDAO letterDAO();

	private static AppDatabase INSTANCE;

	public static AppDatabase getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "LETTERS_DATABASE")
					.allowMainThreadQueries()
					.build();
		}

		return INSTANCE;
	}
}
