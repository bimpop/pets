/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.example.android.pets.data.*;
import com.example.android.pets.data.PetContract.*;
// import android.support.design.widget.FloatingActionButton;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int PET_LOADER = 0;

	/** Database helper that will provide us access to the database */
    private PetDbHelper mDbHelper;

	private ListView mPetsView;

	private PetCursorAdapter mCursorAdapter;
	
	private TextView mEmptyViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
		
		mDbHelper = new PetDbHelper(this);

        // Setup FAB to open EditorActivity
        ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
		

		mPetsView = (ListView) findViewById(R.id.text_view_pet);
		mCursorAdapter = new PetCursorAdapter(this, null);
		mPetsView.setAdapter(mCursorAdapter);
		
		mEmptyViewState = (TextView) findViewById(R.id.empty_view);
		mEmptyViewState.setText(R.string.no_pets);
		mPetsView.setEmptyView(mEmptyViewState);
		
		getLoaderManager().initLoader(PET_LOADER, null, this);
		
		mPetsView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> adapterview, View view, int position, long id){
					Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
					
					Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
					
					intent.setData(currentPetUri);
					
					startActivity(intent);
				}

		});
    }

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle){
		// Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
			PetEntry._ID,
			PetEntry.COLUMN_PET_NAME,
			PetEntry.COLUMN_PET_BREED
		};
		/*
		 * Takes actiin bases on the ID of the Loadee that's being created
		 */
		switch (loaderId){
			case PET_LOADER:
				// Returns a new cursor loader
				return new CursorLoader(this,	// parent activity context
					PetEntry.CONTENT_URI,		// provider content URI to query
					projection,					// columns to include in the result
					null,						// no selection clause
					null,						// no selection arguments
					null);						// default sort order
			default:
				// An invalid id was passed in
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
		mCursorAdapter.swapCursor(cursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader){
		mCursorAdapter.changeCursor(null);
	}


    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertPet() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the pets table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        Uri newRowUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
		long newRowId = ContentUris.parseId(newRowUri);
		
		if(newRowId == -1){
			Toast.makeText(this, R.string.toast_dummy_error, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.toast_dummy, Toast.LENGTH_SHORT).show();
		}
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
				// Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
				// Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // delete all pets
				deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
