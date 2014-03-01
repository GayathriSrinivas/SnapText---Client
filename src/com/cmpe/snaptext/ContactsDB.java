package com.cmpe.snaptext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class ContactsDB extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactManager";
    private static final String CONTACTS_TABLE = "contact";
    private static final String CONTACTS_MSG_TABLE = "contact_messages";
 
    // contact Table Columns names
    private static final String CONTACT_ID = "cid";
    private static final String CNAME = "cname";
    private static final String PHONE_NO = "phone_number";
    
    //contact_messages column names
    private static final String MESSAGE = "message";
    private static final String RECEIVED = "received";
    private static final String CTIME = "ctime";
    
    //One Time Instantiation 
    static ContactsDB instance = null;
    static SQLiteDatabase db = null;
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + CONTACTS_TABLE + "("
                + CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    		    + CNAME + " TEXT, "
                + PHONE_NO + " TEXT "
    		    + ")";
       	Log.i("ContactsDB String",CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);	
        
        String CREATE_CONTACTS_MSG_TABLE = "CREATE TABLE IF NOT EXISTS " + CONTACTS_MSG_TABLE + " ( "
        		+ CONTACT_ID + " INTEGER, "
        		+ RECEIVED + " INTEGER, "
        		+ CTIME + " DATE DEFAULT (datetime('now','localtime')), "
        		+ MESSAGE + " TEXT "
        		+ ")";
        Log.i("ContactsMsgDB String",CREATE_CONTACTS_MSG_TABLE);
        db.execSQL(CREATE_CONTACTS_MSG_TABLE);
	}
    
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE);
        onCreate(db);	
	}
	
    public static void init(Context context) {
        if (null == instance) {
            instance = new ContactsDB(context);
        }
    }
    
    public static void deactivate() {
        if (null != db && db.isOpen()) {
            db.close();
        }
        db = null;
        instance = null;
    }
    
    public static SQLiteDatabase getDb() {
        if (null == db) {
            db = instance.getWritableDatabase();
        }
        return db;
    }
    
	public ContactsDB(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);	
	}
		
	public static void addContact(String name , String phoneNumber){
	    SQLiteDatabase db = getDb();
	    try{
			String query = "SELECT cid FROM " + CONTACTS_TABLE + " WHERE " + PHONE_NO + "=" + phoneNumber ;
			Cursor cursor = getDb().rawQuery(query, null);
			Log.e("asdf", "count " + cursor.getCount());
			if(cursor.getCount() > 0)
				return;
		    ContentValues values = new ContentValues();
		    values.put(CNAME, name); 
		    values.put(PHONE_NO, phoneNumber);
		    db.insert(CONTACTS_TABLE, null, values);
	    }
	    finally{
	    	 db.close();
	    }  
	}
	
	public static Cursor getContactNames(){
		String query = "SELECT cid as _id, cname, phone_number  FROM " + CONTACTS_TABLE ;
		return getDb().rawQuery(query, null);
	}
	
	public static String getContactName(Context context,String phoneNumner) {
		String contactName;
		init(context);
		String query = "SELECT " +CNAME + " FROM " + CONTACTS_TABLE + " WHERE " + PHONE_NO + " = " + phoneNumner;
		Cursor cursor = getDb().rawQuery(query, null);
		cursor.moveToFirst();
		Log.i("DB", "cname column index " + cursor.getColumnIndex("cname"));
		Log.i("DB",cursor.getString(0) + ":" + cursor.getCount());
		contactName = cursor.getString(0);
		deactivate();
		return contactName;
	}
	public static Cursor getContactMessages(String phoneNumber) {
		String query = "SELECT " + MESSAGE + "," + RECEIVED + " FROM " + CONTACTS_MSG_TABLE + " WHERE " 
						+ CONTACT_ID + "= ( SELECT "+ CONTACT_ID + " FROM " + CONTACTS_TABLE +
						" WHERE " + PHONE_NO +"="+phoneNumber + ") ORDER BY " + CTIME ;
		Log.i("query", query);
		
		return getDb().rawQuery(query, null);
	}
	public static void addContactMessage(Context context, String phoneNumber,String message,boolean received){
		init(context);
		int cid ;
		SQLiteDatabase db = getDb();	    	   
		
		String query = "SELECT cid FROM " + CONTACTS_TABLE + " WHERE " + PHONE_NO + "=" + phoneNumber;
		Cursor cursor = getDb().rawQuery(query, null);	
		if (cursor.getCount() == 0) {
			String name = PhoneContacts.getInstance().getContactNameFromNumber(context, phoneNumber);
		    ContentValues values = new ContentValues();
		    values.put(CNAME, name);
		    values.put(PHONE_NO, phoneNumber);
		    db.insert(CONTACTS_TABLE, null, values);
			cursor = getDb().rawQuery(query, null);
		}
		// cursor is guaranteed to have a row at least.
		cursor.moveToFirst();
		Log.e("DB", "cid column index " + cursor.getColumnIndex("cid"));
		Log.i("DB",cursor.getInt(0) + ":" + cursor.getCount());
		cid = Integer.parseInt(cursor.getString(0));
		
		ContentValues values = new ContentValues();
		values.put(CONTACT_ID, cid);
	    values.put(MESSAGE, message); 
	    values.put(RECEIVED, received ? 1 : 0);
	    db.insert(CONTACTS_MSG_TABLE, null, values);
	    db.close();
	    deactivate();
	}
}
