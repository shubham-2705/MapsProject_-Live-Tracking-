package com.tracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.Api;
import com.tracker.utils.ApiConstants;
import com.tracker.utils.TrackerPreferences;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by shubhamlamba on 2/16/18.
 */

public class TimerService extends Service {

    public CountDownTimer countDownTimer;
    private long timeSpent = 0;
    private long totalTime = 10 * 60 * 60 * 1000; // 10 hours * 60 * 60 sec


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            if (Math.abs(TrackerPreferences.getInstance(TimerService.this).getLong(ApiConstants.Preferenceconstants.USER_TIME_SPENT)) > 0) {
                timeSpent = TrackerPreferences.getInstance(TimerService.this).getLong(ApiConstants.Preferenceconstants.USER_TIME_SPENT);
            }

            countDownTimer = new CountDownTimer(totalTime, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    if (TrackerPreferences.getInstance(TimerService.this).getBoolean(ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE)) {
                        countDownTimer.cancel();
                        stopSelf();
                        return;
                    }
//                timeSpent = ((totalTime) - (millisUntilFinished));

                    timeSpent = timeSpent - 1;
                    Log.d("=======", ""+timeSpent);
                    EventBus.getDefault().post(timeSpent);
                }

                @Override
                public void onFinish() {
                }
            };

            countDownTimer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        TrackerPreferences.getInstance(this).setLong(ApiConstants.Preferenceconstants.USER_TIME_SPENT, timeSpent);
    }
}
