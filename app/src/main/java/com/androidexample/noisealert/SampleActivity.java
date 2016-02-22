package com.androidexample.noisealert;

import android.os.Bundle;
import android.widget.TextView;

public class SampleActivity extends BlowDetectionActivity {

    /* References to view elements */
    private TextView mTvStatus;
    private SoundLevelView mSoundLevelView;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Defined SoundLevelView in main.xml file
        setContentView(R.layout.main);
        mTvStatus = (TextView) findViewById(R.id.status);
        mSoundLevelView = (SoundLevelView) findViewById(R.id.volume);
    }


    @Override
    public void onResume() {
        super.onResume();
        mSoundLevelView.setLevel(0, mThreshold);
    }

    protected void updateDisplay(String status, double signalEMA) {
        mTvStatus.setText(status + " signalEMA:" + signalEMA);
        //
        mSoundLevelView.setLevel((int) signalEMA, mThreshold);
    }
};
