package com.cmpe.snaptext;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ComposeMsgActivity extends Activity implements OnItemClickListener{
	private ListView listview;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private Intent sendMsgIntent;
	private PhoneContacts phoneContacts;
	private JSONObject contactList;
	private ProgressDialog dialog ;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose_msg);
		getActionBar().setTitle("Select Contact");
		listview = (ListView) findViewById(R.id.lv_sqlite);
		
		listview.setOnItemClickListener(this);
		ContactsDB.init(this);
		cursor = ContactsDB.getContactNames();
		String fields[] = { "cname","phone_number"};
		int ids[] = { R.id.tv_name , R.id.tv_phone };
		adapter = new SimpleCursorAdapter(this,R.layout.list_view_item,cursor,fields,ids );	
		listview.setAdapter(adapter);
		ContactsDB.deactivate();
		phoneContacts = PhoneContacts.getInstance();
		dialog = new ProgressDialog(this);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
		case R.id.action_refresh:
			String msg = "Please Wait.Snap Text Is Refreshing Contact List";
	        contactList = phoneContacts.getPhoneContactList(this);
	        new SendContactListToServer(dialog, contactList,this,msg).execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
    	
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String name = ((TextView)view.findViewById(R.id.tv_name)).getText().toString();
		String phone = ((TextView)view.findViewById(R.id.tv_phone)).getText().toString();
		Log.i("ADAPTER", name);
		Log.i("ADAPTER", phone);
		Toast.makeText(getApplicationContext(),name + ":" + phone, Toast.LENGTH_LONG).show();
		sendMsgIntent = new Intent(this,SendMsgActivity.class);
		sendMsgIntent.putExtra("name", name);
		sendMsgIntent.putExtra("phone", phone);
		startActivity(sendMsgIntent);
	}	
}
