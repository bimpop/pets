package com.example.android.pets;

import android.widget.*;
import android.view.*;
import android.database.*;
import android.content.*;
import com.example.android.pets.data.PetContract.PetEntry;

public class PetCursorAdapter extends CursorAdapter
{
	
	public PetCursorAdapter(Context context, Cursor c){
		super(context, c, 0);
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent){
		return LayoutInflater.from(context).inflate(R.layout.list_item_pet, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor c){
		// Fields to populate in inflated template
		TextView nameTextView = (TextView) view.findViewById(R.id.list_item_name);
		TextView breedTextView = (TextView) view .findViewById(R.id.list_item_breed);
		
		// Extract properties from cursor
		// Find the columns of the pet attributes that we are interested in
		int nameColumnIndex = c.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME);
		int breedColumnIndex = c.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED);
		
		// Read the pet attributes from the cursor for the current pet
		String name = c.getString(nameColumnIndex);
		String breed = c.getString(breedColumnIndex);
		
		// Populate the fields with the extracted properties
		nameTextView.setText(name);
		breedTextView.setText(breed);
	}

}
