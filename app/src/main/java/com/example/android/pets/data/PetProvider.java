package com.example.android.pets.data;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.*;
import com.example.android.pets.data.PetContract.*;
import android.text.*;

public class PetProvider extends ContentProvider
{
	// Tag for log messages
	public static final String LOG_TAG = PetProvider.class.getSimpleName();

	// declare the PetDbHelper object
	private PetDbHelper mDbHelper;
	
	// Uri match code for general table operation
	private static final int PETS = 100;
	
	// Uri match code for single row operation
	private static final int PETS_ID = 101;
	
	// Creates a UriMatcher object
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
		
		sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
	}
	
	@Override
	public boolean onCreate(){
		// instantiate the PetDbHelper class
		mDbHelper = new PetDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
		// Get readable database
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		// The cursor will hold the result of the query
		Cursor cursor;
		
		// Figure out if the URI matcher can match the URI to a specific code
		int match = sUriMatcher.match(uri);
		switch (match) {
			case PETS:
				cursor = db.query(
					PetEntry.TABLE_NAME,
					projection,
					selection,
					selectionArgs,
					null,
					null,
					sortOrder);
				break;
			case PETS_ID:
				selection = PetEntry._ID + "=?";
				selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
				cursor = db.query(
					PetEntry.TABLE_NAME,
					projection,
					selection,
					selectionArgs,
					null,
					null,
					sortOrder);
				break;
			default:
				throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
		}
		
		// Set notification URI on the cursor.
		// so we know what content URI the cursor was created for.
		// If the data at this URI changes, then we know we need to update the cursor.
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		// return the cursor
		return cursor;
	}

	@Override
	public String getType(Uri uri){
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case PETS:
				return PetEntry.CONTENT_LIST_TYPE;
			case PETS_ID:
				return PetEntry.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values){
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case PETS:
				return insertPet(uri, values);
			default:
				throw new IllegalArgumentException("Cannot insert unknown URI: " + uri);
		}
	}
	
	private Uri insertPet(Uri uri, ContentValues values){
		// Check that the name is not null
		String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
		if(name == null){
			throw new IllegalArgumentException("Pet requires a name");
		}
		
		// Check that the gender is valid
		Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
		if(gender == null || !PetEntry.isValidGender(gender)){
			throw new IllegalArgumentException("Pet requires a valid gender");
		}
		
		// If the weight is provided, check that it's greater than or equal to 0 kg
		Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
		if(weight != null && weight < 0){
			throw new IllegalArgumentException("Pet requires a valid weight");
		}
		
		// no need to check breed, any value is valid (including null)
		
		// Get writeable database
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// The long will hold the row id of the insert
		long newRowId;
		newRowId = db.insert(
			PetEntry.TABLE_NAME,
			null,
			values
		);
		
		// Notify all listeners that the data has changed for the pet content URI
		// uri: content://com.example.android.pets/pets
		getContext().getContentResolver().notifyChange(uri, null);
		
		// Return the new URI with the ID (of the newly inserted row) appended at the end
		return ContentUris.withAppendedId(uri, newRowId);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs){
		final int match = sUriMatcher.match(uri);
		switch(match){
            case PETS:
                // Delete all rows that match the selection and selection args
                return deletePet(uri, selection, selectionArgs);
			case PETS_ID:
				return deletePet(uri, selection, selectionArgs);
			default:
				throw new IllegalArgumentException("Cannot delete unknown URI: " + uri);
		}
	}
	
	private int deletePet(Uri uri, String selection, String[] selectionArgs){
		// Get writeable database
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// The long will hold the row id of the insert
		int rowsDeleted;
		
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case PETS:
				// Delete all rows that match tge selection and selection args
				rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case PETS_ID:
				// Delete a single row given by the ID in the URI
				selection = PetEntry._ID + "=?";
				selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
				rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Deletion is not supporred for " + uri);
		}

		if(rowsDeleted != 0){
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
		final int match = sUriMatcher.match(uri);
		switch(match){
			case PETS:
				return updatePet(uri, values, selection, selectionArgs);
			case PETS_ID:
				selection = PetEntry._ID + "=?";
				selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
				return updatePet(uri, values, selection, selectionArgs);
			default:
				throw new IllegalArgumentException("Cannot update unknown URI: " + uri);
		}
	}
	
	private int updatePet(Uri uri, ContentValues values, String selection,String[] selectionArgs){
		// Check that the name is not null
		if(values.containsKey(PetEntry.COLUMN_PET_NAME)){
			String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
			if(name == null){
				throw new IllegalArgumentException("Pet requires a name");
			}
		}
		
		// Check that the gender is valid
		if(values.containsKey(PetEntry.COLUMN_PET_NAME)){
			Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
			if(gender == null || !PetEntry.isValidGender(gender)){
				throw new IllegalArgumentException("Pet requires a valid gender");
			}
		}

		// If the weight is provided, check that it's greater than or equal to 0 kg
		if(values.containsKey(PetEntry.COLUMN_PET_NAME)){
			Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
			if(weight != null && weight < 0){
				throw new IllegalArgumentException("Pet requires a valid weight");
			}
		}

		// no need to check breed, any value is valid (including null)

		// Get writeable database
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		int rowsUpdated = db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

		if(rowsUpdated != 0){
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rowsUpdated;
	}

}
