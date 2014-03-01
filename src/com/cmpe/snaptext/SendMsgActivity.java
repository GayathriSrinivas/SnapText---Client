package com.cmpe.snaptext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SendMsgActivity extends Activity implements OnClickListener{
	
	private Button btn_send;
	private EditText et_message;
	private String receiverNumber;
	private MessageAdapter adapter;
	private ListView listview;
	private String senderNumber;
	private SharedPreferences sharedPrefereces;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_msg);
		getActionBar().setTitle(getIntent().getStringExtra("name"));
		receiverNumber = getIntent().getStringExtra("phone");
		btn_send = (Button) findViewById(R.id.btn_send);
		listview = (ListView) findViewById(R.id.messageHistoryList);
		et_message = (EditText) findViewById(R.id.et_message);
		btn_send.setOnClickListener(this);
		sharedPrefereces = getSharedPreferences(RegisterDevice.MyPREFERENCES, Context.MODE_PRIVATE);
		populateMessages();
		
		// BroadcastReceiver to receive notifications
		BroadcastReceiver notifier = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (!intent.getStringExtra("sender").equalsIgnoreCase(receiverNumber))
					return;
				adapter.add(new Message("", true, intent.getStringExtra("msg")));
                adapter.notifyDataSetChanged();
                listview.setSelection(adapter.getCount() - 1);
			}
		};
		//TODO : Call Unregister for this
		// Tie the broadcast receiver to GCM receiver
		IntentFilter notifyFilter = new IntentFilter(GcmBroadcastReceiver.NOTIFIER_INTENT);
        registerReceiver(notifier, notifyFilter);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_send){
			String msg = et_message.getText().toString();
			senderNumber = sharedPrefereces.getString("phoneNumber"," ");
			ContactsDB.addContactMessage(this, receiverNumber, msg, false);
			new SendMsgToServer(msg,receiverNumber,senderNumber).execute();
			adapter.add(new Message("", false, msg));
			adapter.notifyDataSetChanged();
			listview.setSelection(adapter.getCount() - 1);
			et_message.setText("");
		}
	}
	
	private void populateMessages() {
		ContactsDB.init(this);
		Cursor cursor = ContactsDB.getContactMessages(receiverNumber);
		adapter = new MessageAdapter(this, R.layout.listview_row);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				adapter.add(new Message("", cursor.getInt(1) == 1, cursor.getString(0)));
			} while (cursor.moveToNext());
		}
		listview.setAdapter(adapter);
		listview.setSelection(adapter.getCount() - 1);
		ContactsDB.deactivate();
	}
}

class MessageAdapter extends ArrayAdapter<Message> {
	
	private ArrayList<Message> messages;
	
	public MessageAdapter(Context context, int resource) {
		super(context, resource);
		messages = new ArrayList<Message>();
	}
	
	public int getCount() {
		return messages.size();
	}
	
	public Message getItem(int index) {
		return messages.get(index);
	}
	
	public void add(Message message) {
		messages.add(message);	
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listview_row, parent, false);
		}
		Message message = messages.get(position);
		TextView tv = (TextView)row.findViewById(R.id.tv_message);
		tv.setBackgroundResource(message.received ? R.drawable.bubble_yellow : R.drawable.bubble_green);
		tv.setText(message.message);
		((LinearLayout)row.findViewById(R.id.wrapper)).setGravity(message.received ? Gravity.LEFT : Gravity.RIGHT);
		return row;
	}
	
}

class SendMsgToServer extends AsyncTask<Void,Void,Void>{
	String msg;
	String receiverNumber;
	String senderNumber;
	
	public SendMsgToServer(String msg, String receiverNumber,String senderNumber) {
		this.msg = msg;
		this.senderNumber = senderNumber;
		this.receiverNumber = receiverNumber;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected Void doInBackground(Void... arg0) {
        try {
			URL url = new URL("http://snaptext.foamsnet.com:5000/send_message?message="+URLEncoder.encode(msg)
							+"&sender_number="+senderNumber+"&receiver_number=" +receiverNumber);
			URLConnection connection;
			connection = url.openConnection();
			connection.connect();			
			// download the file
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        Log.i("TAG",br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	
}

