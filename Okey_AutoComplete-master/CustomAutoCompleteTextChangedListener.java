package com.example.autocompletetextviewdb;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;

public class CustomAutoCompleteTextChangedListener implements TextWatcher {

	public static final String TAG = "CustomAutoCompleteTextChangedListener.java";
	Context context;

	public CustomAutoCompleteTextChangedListener(Context context) {
		this.context = context;
	}


	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence userInput, int start, int before,
			int count) {

		Log.e(TAG, "User input: " + userInput);

		MainActivity mainActivity = ((MainActivity) context);

		mainActivity.item = mainActivity.getItemsFromDb(userInput.toString());
		mainActivity.myAdapter.notifyDataSetChanged();
		mainActivity.myAdapter = new ArrayAdapter<String>(mainActivity,
				android.R.layout.simple_dropdown_item_1line, mainActivity.item);
		mainActivity.myAutoComplete.setAdapter(mainActivity.myAdapter);
	}
}