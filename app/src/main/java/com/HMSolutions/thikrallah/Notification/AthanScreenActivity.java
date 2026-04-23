package com.HMSolutions.thikrallah.Notification;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;

public class AthanScreenActivity extends AppCompatActivity {

    private static final int AUTO_DISMISS_DELAY = 10 * 60 * 1000; // 10 دقايق
    private Handler autoHandler = new Handler();
    private String dataType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // اظهر الشاشة فوق شاشة القفل
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }

        setContentView(R.layout.activity_athan_screen);

        dataType = getIntent().getStringExtra("com.HMSolutions.thikrallah.datatype");

        // اعرض اسم الصلاة
        TextView prayerNameText = findViewById(R.id.prayer_name_text);
        TextView athanText = findViewById(R.id.athan_text);
        Button stopButton = findViewById(R.id.stop_athan_button);

        String prayerName = getPrayerName(dataType);
        prayerNameText.setText(prayerName);
        athanText.setText("حان وقت صلاة " + prayerName);

        // زرار إيقاف الأذان
        stopButton.setOnClickListener(v -> stopAthanAndClose());

        // شغل الأذان
        playAthan();

        // اقفل الشاشة تلقائياً بعد 10 دقايق
        autoHandler.postDelayed(this::stopAthanAndClose, AUTO_DISMISS_DELAY);
    }

    private String getPrayerName(String dataType) {
        if (dataType == null) return "الصلاة";
        switch (dataType) {
            case MainActivity.DATA_TYPE_ATHAN1: return "الفجر";
            case MainActivity.DATA_TYPE_ATHAN2: return "الظهر";
            case MainActivity.DATA_TYPE_ATHAN3: return "العصر";
            case MainActivity.DATA_TYPE_ATHAN4: return "المغرب";
            case MainActivity.DATA_TYPE_ATHAN5: return "العشاء";
            default: return "الصلاة";
        }
    }

    private void playAthan() {
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
        data.putString("com.HMSolutions.thikrallah.datatype", dataType);
        data.putBoolean("isUserAction", false);

        Intent intent = new Intent(this, ThikrService.class).putExtras(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopAthanAndClose() {
        // وقف الأذان
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_STOP);
        Intent intent = new Intent(this, ThikrService.class).putExtras(data);
        startService(intent);

        autoHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        autoHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
