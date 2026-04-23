package com.HMSolutions.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.HMSolutions.thikrallah.MainActivity;

public class ThikrAlarmReceiver extends BroadcastReceiver {
    String TAG = "ThikrAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onrecieve called");

        // لا تشغل الأذكار الصوتية أثناء المكالمات
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null && tm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            Log.d(TAG, "Call in progress, skipping thikr");
            return;
        }

        Bundle data = intent.getExtras();
        if (data == null) return;

        String dataType = data.getString("com.HMSolutions.thikrallah.datatype");

        // لو الأذان افتح شاشة الأذان
        if (isAthanType(dataType)) {
            Intent athanIntent = new Intent(context, AthanScreenActivity.class);
            athanIntent.putExtras(data);
            athanIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(athanIntent);
        } else {
            // باقي التنبيهات تشتغل عادي
            data.putBoolean("isUserAction", false);
            Intent intent2 = new Intent(context, ThikrService.class).putExtras(data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "starting foreground service ThikrService");
                context.startForegroundService(intent2);
            } else {
                Log.d(TAG, "starting background service ThikrService");
                context.startService(intent2);
            }
        }
    }

    private boolean isAthanType(String dataType) {
        if (dataType == null) return false;
        return dataType.equals(MainActivity.DATA_TYPE_ATHAN1) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN2) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN3) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN4) ||
               dataType.equals(MainActivity.DATA_TYPE_ATHAN5);
    }
}
