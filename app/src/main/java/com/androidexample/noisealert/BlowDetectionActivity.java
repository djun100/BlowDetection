package com.androidexample.noisealert;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by cy on 2016/2/22.
 */
public abstract class BlowDetectionActivity extends Activity{
    /* 音频输入采样间隔 */
    protected static final int POLL_INTERVAL = 300;
    /**吹气最短有效时间，单位毫秒*/
    protected static final int DURATION_AVAILABLE = 1000;
    /**running state*/
    protected boolean mRunning = false;
    /** 吹气触发界限*/
    protected static int mThreshold=3;

    protected PowerManager.WakeLock mWakeLock;

    protected Handler mHandler = new Handler();
    /* sound data source */
    protected SoundMeter mSoundMeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSoundMeter = new SoundMeter();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mRunning) {
            mRunning = true;
            start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop noise monitoring
        stop();
    }

    /**Define runnable thread again and again detect noise*/
    private Runnable mSleepTask = new Runnable() {
        public void run() {
            start();
        }
    };
    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        double record = 0;
        long timeStart = 0;
        long timeEnd = 0;
        long duration = 0;

        public void run() {

            double amp = mSoundMeter.getAmplitude();
            //Log.i("Noise", "runnable mPollTask");
            if (amp > mThreshold) {
                if (timeStart == 0) {
                    timeStart = System.currentTimeMillis();
                }
            } else {
                if (timeEnd == 0) {
                    timeEnd = System.currentTimeMillis();
                }
                if (timeEnd != 0 && timeStart != 0) {
                    duration = timeEnd - timeStart;
                }
                if (duration < DURATION_AVAILABLE) {//吹气时间小于1秒无效
                    timeEnd = timeStart = duration = 0;
                } else {//公布结果
                    Log.w("", "公布结果");
                    record = duration / 1000;
                    //清空临时值
                    timeEnd = timeStart = duration = 0;
                }
            }
            updateDisplay("Monitoring Voice..." + record + "秒", amp);

            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };
    private void start() {
        mSoundMeter.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        Log.i("Noise", "==== Stop Noise Monitoring===");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSoundMeter.stop();
//        mSoundLevelView.setLevel(0, 0);
        updateDisplay("stopped...", 0.0);
        mRunning = false;

    }
    protected abstract void updateDisplay(String status, double signalEMA) ;
}
