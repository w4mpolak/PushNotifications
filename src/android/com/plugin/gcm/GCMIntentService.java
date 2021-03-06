package com.plugin.gcm;

import com.google.android.gcm.GCMBaseIntentService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

    public static final int NOTIFICATION_ID = 237;

    private static String TAG = "PushPlugin-GCMIntentService";

    public static final String MESSAGE = "message";

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    public void onRegistered(Context context, String regId) {
        Log.d(TAG, "onRegistered: " + regId);
        NotificationService.getInstance(context).onRegistered(regId);
    }

    @Override
    public void onUnregistered(Context context, String regId) {
        Log.d(TAG, "onUnregistered - regId: " + regId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
//        boolean isAppInForeground = NotificationService.getInstance(context).isForeground();

        Bundle extras = intent.getExtras();
        if (extras != null) {
            // If in background, create notification to display in notification center
//            if (!isAppInForeground) {
//                if (extras.getString(MESSAGE) != null && extras.getString(MESSAGE).length() != 0) {
                    createNotification(context, extras);
//                }
//            }

            NotificationService.getInstance(context).onMessage(extras);
        }
    }

    public void createNotification(Context context, Bundle extras) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("pushBundle", extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int defaults = Notification.DEFAULT_ALL;

        if (extras.getString("defaults") != null) {
            try {
                defaults = Integer.parseInt(extras.getString("defaults"));
            } catch (NumberFormatException e) {
            }
        }
        
        String title   = extras.getString("title"  );
        String header  = extras.getString("header" );
        String message = extras.getString("message");

        if(message == null && title !=null){
        	message = title;
        	title = null;
        }
        
        if(header != null)
        	title = header;
        
        if(title ==  null)
        	title = "mySchaffNet";
        
        if(message == null)
        	message = "<missing message content>";
        
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(defaults)
                        .setSmallIcon(context.getApplicationInfo().icon)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setContentText(message)
        ;
        
        String msgcnt = extras.getString("msgcnt");
        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }

        int notId = NOTIFICATION_ID;

        try {
            notId = Integer.parseInt(extras.getString("notId"));
        } catch (NumberFormatException e) {
            Log.e(TAG,
                    "Number format exception - Error parsing Notification ID: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID" + e.getMessage());
        }

        mNotificationManager.notify((String) appName, notId, mBuilder.build());

    }

    public static void cancelNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel((String) getAppName(context), NOTIFICATION_ID);
    }

    private static String getAppName(Context context) {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String) appName;
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(TAG, "onError - errorId: " + errorId);
    }

	public static void enqueueOnResumeNotification(Context context, Object notification) {
		NotificationService.getInstance(context).enqueueOnResumeNotification(notification);
		
	}

}
