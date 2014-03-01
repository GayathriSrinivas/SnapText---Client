package com.cmpe.snaptext;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

    private EditText et_phoneNo ;
    private Button btn_register;    
    private Intent composeMsgIntent;
    private RegisterDevice registerDevice;
    private PhoneContacts phoneContacts;
    private JSONObject contactList;
    private ProgressDialog dialog ;
    private TextView tv_title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv_title = (TextView)findViewById(R.id.tv_title);
		tv_title.setTextColor(Color.WHITE);
		
		registerDevice = RegisterDevice.getInstance();
		phoneContacts = PhoneContacts.getInstance(); 
        if (registerDevice.getRegistrationId(this) != null) {
	        //Create a new Intent
	        composeMsgIntent = new Intent(getBaseContext(),ComposeMsgActivity.class);
	        startActivity(composeMsgIntent);
	        finish();
	        return;
        }
        
    	dialog = new ProgressDialog(this);
		btn_register = (Button)findViewById(R.id.btn_register);
		btn_register.setOnClickListener(this);
		et_phoneNo=(EditText)findViewById(R.id.et_phoneNo);
		et_phoneNo.setText(phoneContacts.getDevicePhoneNumber(this));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_register){
			SharedPreferences sharedPreferences = getSharedPreferences(RegisterDevice.MyPREFERENCES, Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putString("phoneNumber", et_phoneNo.getText().toString());
			editor.commit();
			registerDevice.checkDeviceRegistered(this);
	        contactList = phoneContacts.getPhoneContactList(this);
	        String msg = "Snap Text is initializing " + "\n" +
	        		"Please wait this could take a few minutes";
	        new SendContactListToServer(dialog, contactList,this,msg).execute();  
		}
	}
}
