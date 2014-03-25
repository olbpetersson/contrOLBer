package com.example.controller;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewActivity extends ListActivity{
		ProfileDatabase db = null;
		String[] BULLETS = {" ", " "};
		SQLiteDatabase dbSQL;
		
		private static final String DATABASE_NAME = "ControllerDB";
		private static final String PROFILE_TABLE = "Profile_Table";
		private static final int DATABASE_VERSION = 1;
		
		public static final String dbID = "ID";
		public  static final String dbName = "NAME";
		public static final String dbIP = "IP";
		public static final String dbPort = "PORT";
		private ListView lv;
		private ArrayAdapter<String> apb;
		Dialog addProfileDialog;
		
		protected void onCreate(Bundle savedInstanceState){
			
			super.onCreate(savedInstanceState);			
		}
		
		
		@Override
		protected void onResume()
		{
		super.onResume();
		    //Restore state here
			db = ProfileDatabase.getDatabase(this);
			
		
			if(db != null){
		
				db.open();
				dbSQL = db.getSQL();
				//dbSQL.execSQL("INSERT INTO " +PROFILE_TABLE+ " (NAME, IP, PORT) VALUES ('hej123225', '123', '123')");
				BULLETS = db.getData(PROFILE_TABLE);

			}
			apb = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, BULLETS);
			
			setListAdapter(apb);
			lv = getListView();
			lv.setTextFilterEnabled(true);
			
			
			lv.setOnItemClickListener(new OnItemClickListener(){
				
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					//Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_LONG).show();
					String ipToStart = getStringFromDB(((TextView) view).getText().toString(), dbIP);
					String portToStart = getStringFromDB(((TextView) view).getText().toString(), dbPort);
					Intent i = new Intent(getApplicationContext(), TheMouse.class);
					i.putExtra("ip", ipToStart);
					i.putExtra("port", portToStart);
					startActivity(i);
				}
			
			
			});
			
			lv.setOnItemLongClickListener(new OnItemLongClickListener() {				

				public boolean onItemLongClick(AdapterView<?> arg0, View v,
						int arg2, long arg3) {
					final TextView profileView = (TextView) v;
					Log.i("LONGCLICKED", "LONGCLICKED");
					final Dialog deleteUpdateDialog = new Dialog(ListViewActivity.this);
					deleteUpdateDialog.setTitle(profileView.getText().toString());
					deleteUpdateDialog.setContentView(R.layout.custom_dialog_layout_update_delete_profile);
					Button deleteEntry = (Button) deleteUpdateDialog.findViewById(R.id.deleteProfileButton);
					Button updateEntry = (Button) deleteUpdateDialog.findViewById(R.id.updateProfileButton);				
					
					deleteEntry.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							deleteFromTable(profileView.getText().toString());
							updateListView();
							deleteUpdateDialog.dismiss();
							Toast.makeText(ListViewActivity.this, "Deleted entry: " +profileView.getText().toString(), Toast.LENGTH_LONG).show();
						}
					});
					
					updateEntry.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							String name = "", ip = "", port = "";
							db.open();
							dbSQL = db.getSQL();
							Cursor c = dbSQL.query(true, PROFILE_TABLE, new String[]{dbName,dbIP,dbPort},
									dbName +" = '" +profileView.getText().toString() +"'", null, null, null, null, null);
							if (c != null){
								deleteUpdateDialog.dismiss();
								c.moveToFirst();								
								name = c.getString(c.getColumnIndex(dbName));							
								ip = c.getString(c.getColumnIndex(dbIP));
								port = c.getString(c.getColumnIndex(dbPort));
								
								createProfileDialog(name, ip, port, true, profileView.getText().toString());
							}
							else
								Toast.makeText(ListViewActivity.this, "Unable to retrieve values from database", Toast.LENGTH_LONG).show();
							dbSQL.close();
						}
					});
					deleteUpdateDialog.show();
					return true;
					
				}
			});
			lv.requestLayout();
			lv.invalidate();
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.add_profile_menu, menu);
		    return super.onCreateOptionsMenu(menu);
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			createProfileDialog("", "", "", false, "");
			
		    return super.onOptionsItemSelected(item);
		    
		}
		
		public void createProfileDialog(String name, String ip, String port, boolean isUpdate, String originalName){
			final Dialog createProfileDialog = new Dialog(this);
			final String prevName = originalName;
			final String toastMessage;
			final boolean update = isUpdate;
			createProfileDialog.setTitle("Add new profile");
			createProfileDialog.setContentView(R.layout.custom_dialog_layout_add_profile);
			Button b = (Button) createProfileDialog.findViewById(R.id.custom_dialog_button);
			final EditText editName =(EditText) createProfileDialog.findViewById(R.id.custom_dialog_name);
			final EditText editIP = (EditText) createProfileDialog.findViewById(R.id.custom_dialog_ip);
			final EditText editPort = (EditText) createProfileDialog.findViewById(R.id.custom_dialog_port);
			if(update){
				createProfileDialog.setTitle("Update profile");
				editName.setText(name);
				editIP.setText(ip);
				editPort.setText(port);
				b.setText("Update");
				toastMessage = "Updated the profile data with: ";
			}
			else
			  toastMessage = "Added Profile with data: ";
			b.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i("TAPPED", "OMG");
					String editNameString = editName.getText().toString(), editIPString = editIP.getText().toString(),
							editPortString = editPort.getText().toString();
					if (editNameString.length() > 0 && editIPString.length() > 0 && editPortString.length() > 0){
						editDatabaseAndUpdateListView(editNameString, editIPString, editPortString, update, prevName);
						createProfileDialog.dismiss();
						Toast.makeText(ListViewActivity.this, toastMessage +editName.getText().toString() +
								":" +editIP.getText().toString() +":" +editPort.getText().toString(), Toast.LENGTH_LONG).show();
					}
					else
						Toast.makeText(ListViewActivity.this, "Malformed input", Toast.LENGTH_SHORT).show();
							
				}
			});
			createProfileDialog.show();
		}
		public String getStringFromDB(String match, String column){
			db.open();
			dbSQL = db.getSQL();
			
			return db.getData("SELECT DISTINCT "+column+" FROM " +PROFILE_TABLE+" WHERE NAME ='" +match+"'", column)[0];
		}	
		
		public void deleteFromTable(String match){
			db.open();
			dbSQL = db.getSQL();
			dbSQL.execSQL("DELETE FROM "+PROFILE_TABLE +" WHERE NAME='"+match+"'");
			dbSQL.close();
		}
		public void editDatabaseAndUpdateListView(String name, String ip, String port, boolean isUpdate, String originalName){
			db.open();
			dbSQL = db.getSQL();
			if(isUpdate)
				dbSQL.execSQL("UPDATE "+PROFILE_TABLE +" SET " +dbName +"='" +name +"'," +dbIP + "='" +ip +"',"+dbPort +"='"
							+port +"' WHERE " +dbName +"='" +originalName +"'");
			else
				dbSQL.execSQL("INSERT INTO " +PROFILE_TABLE+ " ("+dbName+", "+dbIP+", "+dbPort+") VALUES ('"+name+"', '"+
							ip+"', '"+ port +"')");
			
			updateListView();
		}
		
		public void updateListView(){
			db.open();
			BULLETS = db.getData(PROFILE_TABLE);
			setListAdapter(new ArrayAdapter<String>(ListViewActivity.this, android.R.layout.simple_list_item_1, BULLETS));
			dbSQL.close();
		}
		
		@Override
		public void onPause(){
			super.onPause();
			
			db.close();
			dbSQL.close();
		}
}

