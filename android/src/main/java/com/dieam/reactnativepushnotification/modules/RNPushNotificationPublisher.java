package com.dieam.reactnativepushnotification.modules;

import android.app.ActivityManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationPublisher extends BroadcastReceiver {
    final static String NOTIFICATION_ID = "notificationId";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        long currentTime = System.currentTimeMillis();

        Log.i(LOG_TAG, "NotificationPublisher: Prepare To Publish: " + id + ", Now Time: " + currentTime);

        Application applicationContext = (Application) context.getApplicationContext();
        Class intentClass = getMainActivityClass(context);
        if (this.isApplicationInForeground(context)) {
            Intent newIntent = new Intent(context, intentClass);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            newIntent.putExtra("notification", intent.getExtras());
            newIntent.putExtra("foreground", true);
            String notificationIdString = intent.getExtras().getString("id");
            int notificationID = Integer.parseInt(notificationIdString);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationID, newIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send(context, 0, newIntent);
            } catch (PendingIntent.CanceledException e) {
            }
        } else
            new RNPushNotificationHelper(applicationContext)
                    .sendToNotificationCentre(intent.getExtras());
    }

    public Class getMainActivityClass(Context context) {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isApplicationInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.processName.equals(context.getPackageName())) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String d : processInfo.pkgList) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
