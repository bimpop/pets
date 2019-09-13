package com.example.android.pets.data;

import android.content.*;
import android.database.sqlite.*;
import com.example.android.pets.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper
{
	// If you change the database schema, you must increment the database version
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Pets.db";
	
	public PetDbHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}
	
	
	private static final String SQL_CREATE_ENTRIES = 
		"CREATE TABLE " + PetEntry.TABLE_NAME + " ( " +
		PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
		PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, " +
		PetEntry.COLUMN_PET_BREED + " TEXT, " +
		PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, " +
		PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0" +
		" ); ";
	
	private static final String SQL_DELETE_ENTRIES = 
		"DROP TABLE IF EXIST " + PetEntry.TABLE_NAME;
	
}
