package com.example.autocompletetextviewdb;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    CustomAutoCompleteView myAutoComplete;
    ArrayAdapter<String> myAdapter;
    DatabaseHandler databaseH;
    String[] item = new String[]{"please Search.."};

    protected void onCreate(Bundle savedInstanceState) {

        myAsyncTask myAsyncTask;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAsyncTask = new myAsyncTask();
        myAsyncTask.execute();
    }

        public class myAsyncTask extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... strings) {
                try {
                    InputStreamReader inputreader = new InputStreamReader(MainActivity.this.getAssets().open("dic.csv"), "euc-kr");
                    BufferedReader buffereader = new BufferedReader(inputreader);
                    List<String> list = new ArrayList<String>();
                    String line;

                    do {
                        line = buffereader.readLine();
                        list.add(line);
                    } while (line != null);

                    databaseH = new DatabaseHandler(MainActivity.this);
                    databaseH.insert(list);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String s) {
                myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.myautocomplete);

                myAutoComplete
                        .addTextChangedListener(new CustomAutoCompleteTextChangedListener(
                                MainActivity.this));

                myAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line, item);

                myAutoComplete.setAdapter(myAdapter);
            }
        }
//		try {
//			InputStreamReader inputreader = new InputStreamReader(this.getAssets().open("dic.csv"), "euc-kr");
//			BufferedReader buffereader = new BufferedReader(inputreader);
//			List<String> list = new ArrayList<String>();
//			String line;
//
//			do {
//				line = buffereader.readLine();
//				list.add(line);
//			} while(line!=null);
//
//			databaseH = new DatabaseHandler(MainActivity.this);
//
//			databaseH.insert(list);
//
//			myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.myautocomplete);
//
//			myAutoComplete
//					.addTextChangedListener(new CustomAutoCompleteTextChangedListener(
//							this));
//
//			myAdapter = new ArrayAdapter<String>(this,
//					android.R.layout.simple_dropdown_item_1line, item);
//
//			myAutoComplete.setAdapter(myAdapter);
//
//
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

    public String[] getItemsFromDb(String searchTerm) {

        List<MyObject> products = databaseH.read(searchTerm);

        int rowCount = products.size();
        String[] item = new String[rowCount];
        int x = 0;

        for (MyObject record : products) {

            item[x] = record.objectName;
            x++;
        }

        return item;
    }
}