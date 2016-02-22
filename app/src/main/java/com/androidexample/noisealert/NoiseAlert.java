package com.androidexample.noisealert;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class NoiseAlert extends Activity {
    /* 音频输入采样间隔 */
    private static final int POLL_INTERVAL = 300;
    /**吹气最短有效时间，单位毫秒*/
    private static final int DURATION_AVAILABLE = 1000;
    /**
     * running state
     **/
    private boolean mRunning = false;

    /**
     * config state
     **/
    private int mThreshold;

    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    /* References to view elements */
    private TextView mTvStatus;
    private SoundLevelView mSoundLevelView;

    /* sound data source */
    private SoundMeter mSoundMeter;

    /******************
     * Define runnable thread again and again detect noise
     *********/

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");

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
            if (amp > 3) {
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
            if ((amp > mThreshold)) {
                callForHelp();
                //Log.i("Noise", "==== onCreate ===");

            }

            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Defined SoundLevelView in main.xml file
        setContentView(R.layout.main);
        mTvStatus = (TextView) findViewById(R.id.status);

        // Used to record voice
        mSoundMeter = new SoundMeter();
        mSoundLevelView = (SoundLevelView) findViewById(R.id.volume);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
    }


    @Override
    public void onResume() {
        super.onResume();
        //Log.i("Noise", "==== onResume ===");

        initializeApplicationConstants();
        mSoundLevelView.setLevel(0, mThreshold);

        if (!mRunning) {
            mRunning = true;
            start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Log.i("Noise", "==== onStop ===");

        //Stop noise monitoring
        stop();

    }

    private void start() {
        //Log.i("Noise", "==== start ===");

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
        mSoundLevelView.setLevel(0, 0);
        updateDisplay("stopped...", 0.0);
        mRunning = false;

    }


    private void initializeApplicationConstants() {
        // Set Noise Threshold
        mThreshold = 8;

    }

    private void updateDisplay(String status, double signalEMA) {
        mTvStatus.setText(status + " signalEMA:" + signalEMA);
        //
        mSoundLevelView.setLevel((int) signalEMA, mThreshold);
    }


    private void callForHelp() {

        //stop();

        // Show alert when noise thersold crossed
        Toast.makeText(getApplicationContext(), "Noise Thersold Crossed, do here your stuff.",
                Toast.LENGTH_LONG).show();
    }

};
