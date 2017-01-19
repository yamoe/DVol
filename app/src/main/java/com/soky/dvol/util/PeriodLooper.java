package com.soky.dvol.util;

import android.os.Handler;

public class PeriodLooper {

    private final Handler mHandler = new Handler();
    private boolean mIsRunning = false;
    private int mPeriodMesc = 0;
    private Runnable mUserLoop;

    private Runnable mMainLoop = new Runnable() {
        @Override
        public void run() {
            if (!mIsRunning) {
                return;
            }

            mUserLoop.run();

            if (mIsRunning) {
                post();
            }
        }
    };

    public void start(Runnable userLoop, int periodMsec) {
        mUserLoop = userLoop;
        mPeriodMesc = periodMsec;

        if (!mIsRunning) {
            mIsRunning = true;
            post();
        }
    }

    public void stop() {
        if (mIsRunning) {
            mIsRunning = false;
        }
    }

    private void post() {
        mHandler.postDelayed(mMainLoop, mPeriodMesc);
    }


}
