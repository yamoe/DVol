package com.soky.dvol;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.soky.dvol.util.AverageDecibelMeter;
import com.soky.dvol.util.DecibelMeter;
import com.soky.dvol.util.DecibelMeterInterface;
import com.soky.dvol.util.PeriodLooper;
import com.soky.dvol.util.Volume;

import java.util.HashSet;

public class AutoVolumeService extends Service {
    public final String TAG = this.getClass().getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    private HashSet<ServiceCallback> mCallbacks = new HashSet<>();

    private Volume mVolume = new Volume();
    private DecibelMeterInterface mDecibelMeter = new AverageDecibelMeter(10);
    private PeriodLooper mLooper = new PeriodLooper();
    private int mLoopMsec = 1000;        // 몇초마다 Loop 를 실행시킬지 지정

    private int mMaxVolume;              // 안드로이드의 최대 볼륨(기기마다 다름)
    private int mStandardVolume;        // 사용자가 설정하는 기준 볼륨
    private int mStandardAmplitude;     // 사용자가 설정하는 기준 진폭
    private int mStepAmplitude;          // 볼륨 조정을 위한 진폭 단위

    private boolean mIsStarted = false;    // 자동 볼륨 조절 시작 여부


    /**
     * 서비스로 부터 현재 데시벨,진폭,볼륨을 콜백받기 위한 객체
     */
    public static abstract class ServiceCallback {
        public abstract void onResult(int decibel, int amplitude, int volume);
    }


    public AutoVolumeService() {
    }

    public class LocalBinder extends Binder {
        public AutoVolumeService getService() {
            return AutoVolumeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind @@@@@@@@@@@@@@@@@@@@");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate @@@@@@@@@@@@@@@@@@@@");

        mMaxVolume = mVolume.getMax();
        mStepAmplitude = DecibelMeter.MAX_AMPLITUDE / mMaxVolume;   //mStepAmplitude = 1000;

        mDecibelMeter.initialize();
        mLooper.start(loop_, mLoopMsec);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy @@@@@@@@@@@@@@@@@@@@");

        mLooper.stop();
        mDecibelMeter.uninitialize();
    }


    /**
     * 서비스에서 주기적인 작업
     */
    private Runnable loop_ = new Runnable() {
        @Override
        public void run() {

            mDecibelMeter.measure();
            int amplitude = mDecibelMeter.getAmplitude();
            int decibel = mDecibelMeter.getDecibel();
            int newVolume = mVolume.getCurrent();

            if (mIsStarted) {
                /*
                    볼륨 조절
                    decibel은 log 함수라 선형 값인 amplitude(진폭)으로 바로 계산함.
                    설정된 기준 진폭(mStandardAmplitude) 와 현재 진폭의 차를 구한후
                    조절할 볼륨 양을 계산한후 설정된 기준 볼륨(mStandardVolume)에 가감.
                 */

                int diffAmplitude = mStandardAmplitude - amplitude;
                int inc = diffAmplitude / mStepAmplitude;
                newVolume = mStandardVolume - inc;

                if (newVolume < 1) newVolume = 1;
                if (newVolume > mMaxVolume) newVolume = mMaxVolume;

                mVolume.setCurrent(newVolume);
//                Log.d(TAG, "@@@@@@@@@@@ mStandardAmplitude : " + mStandardAmplitude + ", mStepAmplitude : " + mStepAmplitude);
//                Log.d(TAG, "@@@@@@@@@@@ " + decibel + " dB (" + amplitude + "), new_volume : " + new_volume +", diff_amp : " + diff_amp + ", inc : " + inc);
            }

            for (ServiceCallback cb : mCallbacks) {
                cb.onResult(decibel, amplitude, newVolume);
            }


        }
    };

    public void registerCallback(ServiceCallback cb) {
        mCallbacks.add(cb);
    }

    public void unregisterCallback(ServiceCallback cb) {
        mCallbacks.remove(cb);
    }

    public void startControlVolume(int volume, int amplitude) {
        mStandardVolume = volume;
        mStandardAmplitude = amplitude;
        mIsStarted = true;
    }

    public void stopControlVolume() {
        mIsStarted = false;
    }

    public boolean isStartedAutoVolume() {
        return mIsStarted;
    }

    public int getAmplitude() {
        return mDecibelMeter.getAmplitude();
    }

    public int getDecibel() {
        return mDecibelMeter.getDecibel();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind @@@@@@@@@@@@@@@@@@@@");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind @@@@@@@@@@@@@@@@@@@@");
        super.onRebind(intent);
    }
}

