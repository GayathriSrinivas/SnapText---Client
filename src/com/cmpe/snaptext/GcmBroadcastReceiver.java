package com.cmpe.snaptext;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	
	public static final String NOTIFIER_INTENT = "com.cmpe.snaptext.intent.action.NOTIFY";
	
    @Override
    public void onReceive(Context context, Intent intent) {	
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // update the UI if active
        String msg = intent.getStringExtra("message");
        String senderNumber = intent.getStringExtra("sender");
        Intent notifier = new Intent(NOTIFIER_INTENT);
        notifier.putExtra("sender", senderNumber);
        notifier.putExtra("msg", msg);
        context.sendBroadcast(notifier);
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
    
}
