package com.cmpe.snaptext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegisterDevice {

	private static String TAG = "Register Device To GCM";
    private SharedPreferences sharedPreferences;
    public final static String MyPREFERENCES = "SnapText" ;
    private static final String PROPERTY_REG_ID = "registration_id";
    private GoogleCloudMessaging gcm;
    private String regid;
    private String SENDER_ID = "561119730721";
    private static RegisterDevice instance;
    private String phoneNumber;
    
    private RegisterDevice() {
    	/*Following Singleton Pattern*/
    }
    
    static {
    	instance = null;
    }
    
    public static synchronized RegisterDevice getInstance() {
    	if(instance == null){
    		Log.i(TAG, "I AM HERE");
    		instance = new RegisterDevice();
    	}
    	return instance;
    }
    
	public void sendRegistrationIdToBackend(Context context) {
		try {
			sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
			phoneNumber = sharedPreferences.getString("phoneNumber", " ");	
			URL url = new URL("http://snaptext.foamsnet.com:5000/registration?regid="+ regid +"&phoneNo=" + phoneNumber);
			URLConnection connection;
			connection = url.openConnection();
			connection.connect();			
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        Log.i(TAG,br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void checkDeviceRegistered(Context context) {
		gcm = GoogleCloudMessaging.getInstance(context);
        regid = getRegistrationId(context);
        if (regid == null) {
            registerInBackground(context);
        }	
	}
	
	public String getRegistrationId(Context context) {
		sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		return sharedPreferences.getString(PROPERTY_REG_ID, null);
	}
	
	public void storeRegistrationId(Context context) {
		sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(PROPERTY_REG_ID, regid);
		editor.commit();
	}
	
	
	public void registerInBackground(final Context context) {
	    new AsyncTask<Void, Void, String>(){
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;
	                Log.i(TAG,msg);
	                sendRegistrationIdToBackend(context);
	                storeRegistrationId(context);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	        	
	        }
	    }.execute(null, null, null);
	}
}
