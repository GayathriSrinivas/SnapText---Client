package com.cmpe.snaptext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneContacts {

	private TelephonyManager mTelephonyMgr;
	private JSONObject contactList;
	private String TAG = getClass().getName();
	private static PhoneContacts instance ;
    private Editor editor;
    private SharedPreferences sharedPreferences;
	
	private PhoneContacts() {
		
	}
	
	public static PhoneContacts getInstance() {
		if( instance == null){
			instance = new PhoneContacts(); 
		}
		return instance;
	}
	
	public String getDevicePhoneNumber(Context context){
		/*Get the Device's Phone No */
		mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = mTelephonyMgr.getLine1Number();
		return  phoneNumber;
	}
	
	public String getContactNameFromNumber(Context context, String phoneNumber) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = context.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getString(0);
		}
		return "";
	}
	
	public JSONObject getPhoneContactList(Context context) {
		
		try {
			contactList = new JSONObject();
			contactList.put("ContactList", new JSONArray() );
			JSONObject contact ; 
			JSONArray array = (JSONArray) contactList.get("ContactList");
			Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
			while (phones.moveToNext() )
			{
			  String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			  contact = new JSONObject(); 
			  phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
			 
			  if(phoneNumber.charAt(0) == '+'){
				phoneNumber = phoneNumber.substring(1);
			  }
				  
			  if(phoneNumber.length() == 10){
				  String countryCode = "1";
				  phoneNumber = countryCode + phoneNumber;
			  }
			  
			  contact.put("name",name);
			  contact.put("phoneNumber", phoneNumber);
			  array.put(contact);
			 // Log.i(name , phoneNumber);
			}
			
			Log.i(TAG,"Total No of Contact :: " + array.length());
			phones.close();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return contactList;
	}
}
