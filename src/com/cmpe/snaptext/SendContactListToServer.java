package com.cmpe.snaptext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

public class SendContactListToServer extends AsyncTask<Void, Void, Void> {

	private ProgressDialog dialog ;
	private String TAG = getClass().getName();
	private JSONObject contactList;
	private Context context;
	private String msg ;
	
	public SendContactListToServer(ProgressDialog dialog,JSONObject contactList,Context context,String msg){
		this.dialog = dialog;
		this.contactList = contactList;
		this.context = context;
		this.msg = msg;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
			dialog.setMessage(msg);
			dialog.show();
	}
	
	@Override
	protected Void doInBackground(Void... params) {
        Looper.prepare(); //For Preparing Message Pool for the child Thread
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 1000000); //Timeout Limit
        HttpResponse response;
        try {
        	String url = new String("http://snaptext.foamsnet.com:5000/contactList");
            HttpPost post = new HttpPost(url);
            StringEntity se = new StringEntity(contactList.toString());  
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = client.execute(post);
            StringBuilder sb= new StringBuilder();
            /*Checking response */
            if(response!=null){
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                
                String line = br.readLine();
                while(line != null){
                	Log.i(TAG,line);
                	sb.append(line);
                	line = br.readLine();
                }
                
                JSONArray array = new JSONArray(sb.toString());

                for (int i = 0; i < array.length(); i++) {
                	ContactsDB.init(context);
                	 int index = array.getInt(i);
                	 JSONObject contact = contactList.getJSONArray("ContactList").getJSONObject(index);
                	 String name = contact.getString("name");
                	 String phoneNumber = contact.getString("phoneNumber");
                	 Log.i("CONTACT...",name + ":" + phoneNumber);
                	 ContactsDB.addContact(name, phoneNumber); 
                	 ContactsDB.deactivate();
				}
            }else
	           Log.i(TAG,"Response is NULL ...but SENT !!!");
        } catch(Exception e) {
            e.printStackTrace();
        }
       Log.i(TAG, "Background task over");
	  return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {

		if(dialog.isShowing()){
			dialog.dismiss();
		}	
        Intent composeMsgIntent = new Intent(context,ComposeMsgActivity.class);
        context.startActivity(composeMsgIntent);
        super.onPostExecute(result);
	}

}
