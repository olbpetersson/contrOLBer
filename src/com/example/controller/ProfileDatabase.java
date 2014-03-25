package com.example.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class ProfileDatabase{
	
	private static final String DATABASE_NAME = "ControllerDB";
	private static final String PROFILE_TABLE = "Profile_Table";
	private static final int DATABASE_VERSION = 1;
	
	public static final String dbID = "ID";
	public  static final String dbName = "NAME";
	public static final String dbIP = "IP";
	public static final String dbPort = "PORT";
	
	private DbHelper dbHelp;
	private static Context dbContext;
	private SQLiteDatabase dataB;
	static ProfileDatabase db;
	
	public static ProfileDatabase getDatabase(Context c) {
		// TODO Auto-generated method stub
		
		if(dbContext == null){
			return db = new ProfileDatabase(c);
		}
		else
			return db;
	}
	
	protected ProfileDatabase(Context c){
		this.dbContext = c;
	}

	private static class DbHelper extends SQLiteOpenHelper{
		
		public DbHelper(Context c){
			super(c, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			//IF EXISTS
			db.execSQL("CREATE TABLE " + PROFILE_TABLE + " ( " +
					dbID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					dbName + " TEXT NOT NULL,"
					+dbIP +" TEXT NOT NULL, " + 
					dbPort +" TEXT NOT NULL)"
					
					);
		}

		@Override	
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + PROFILE_TABLE);
			onCreate(db);
		}
	}

	
	public ProfileDatabase open() throws SQLException{
		dbHelp = new DbHelper(dbContext);
		dataB = dbHelp.getWritableDatabase();
		
		return this;
	}
	public void close(){
		dbHelp.close();
	}
	
	public long createEntry(String table, String name, String ip, int port){
		
		ContentValues cv = new ContentValues();
		cv.put(dbName, name);
		cv.put(dbIP, ip);
		cv.put(dbPort, port);
		
		return dataB.insert(table, null, cv);
		
	}
	
	public SQLiteDatabase getSQL(){
		return this.dataB;
	}
	
	public String[] getData(String table){
	
		String[] columns = new String[]{ dbID, dbName };
		
		Cursor c = dataB.query(table, columns, null, null, null, null, dbName);
		
		String[] result = new String[c.getCount()];
		int iID = c.getColumnIndex(dbID);
		int iName = c.getColumnIndex(dbName);
		
		//Log.i("GETDAAAAAAAAAAATA",":"+result[iID]);
		
		
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
		
			result[iID] = c.getString(iName);
			iID++;
			Log.i("ID: ", ""+iID);
		}
		return result;
	}
	public String[] getData(String query, String column){

		Cursor c = dataB.rawQuery(query, null);

		String[] result = new String[c.getCount()];
		int i = 0;
		int iColumn = c.getColumnIndex(column);
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			result[i] = c.getString(iColumn);
			i++;
			
		}	
		return result;
	}
		



}
