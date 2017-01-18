package com.soky.dvol;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.soky.dvol.control.DecibelMeter;
import com.soky.dvol.control.Volume;
import com.soky.dvol.util.PeriodLooper;

import java.util.HashSet;

public class AutoVolumeService extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private final IBinder binder_ = new LocalBinder();
    private HashSet<ServiceCallback> callbacks_ = new HashSet<ServiceCallback>();

    private Volume volume_ = new Volume();;
    private DecibelMeter decibel_meter_ = new DecibelMeter();
    private PeriodLooper looper_ = new PeriodLooper();
    private int loop_msec_ = 1000;

    private int max_volume_;        // 안드로이드의 최대 볼륨(기기마다 다름)
    public int standard_volume_;    // 사용자가 설정하는 기준 볼륨
    public int standard_amplitude_;
    private int step_amplitude_;


    private boolean is_started_ = false;    // 자동 볼륨 조절 시작 여부


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
        return binder_;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate @@@@@@@@@@@@@@@@@@@@");

        max_volume_ = volume_.getMax();
        step_amplitude_ = DecibelMeter.MAX_AMPLITUDE / max_volume_;
        //step_amplitude_ = 1000;
        standard_volume_ = volume_.getCurrent(); // 임의 설정한 기본값

        decibel_meter_.init();
        looper_.start(loop_, loop_msec_);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy @@@@@@@@@@@@@@@@@@@@");

        looper_.stop();
        decibel_meter_.uninit();
    }


    private Runnable loop_ = new Runnable() {
        @Override
        public void run() {

            decibel_meter_.measure();
            int amplitude = decibel_meter_.getAmplitude();
            int decibel = decibel_meter_.getDecibel();
            int new_volume = volume_.getCurrent();

            if (is_started_) {
                /*
                    볼륨 조절
                    decibel은 log 함수라 선형 값인 amplitude(진폭)으로 바로 계산함.
                    설정된 기준 진폭(standard_amplitude_) 와 현재 진폭의 차를 구한후
                    조절할 볼륨 양을 계산한후 설정된 기준 볼륨(standard_volume_)에 가감.
                 */

                int diff_amp = standard_amplitude_ - amplitude;
                int inc = diff_amp / step_amplitude_;
                new_volume = standard_volume_ - inc;

                if (new_volume < 1) new_volume = 1;
                if (new_volume > max_volume_) new_volume = max_volume_;

                volume_.setCurrent(new_volume);
//                Log.d(TAG, "@@@@@@@@@@@ standard_amplitude_ : " + standard_amplitude_ + ", step_amplitude_ : " + step_amplitude_);
//                Log.d(TAG, "@@@@@@@@@@@ " + decibel + " dB (" + amplitude + "), new_volume : " + new_volume +", diff_amp : " + diff_amp + ", inc : " + inc);
            }

            for (ServiceCallback cb : callbacks_) {
                cb.onResult(decibel, amplitude, new_volume);
            }


        }
    };

    public void registerCallback(ServiceCallback cb) {
        callbacks_.add(cb);
    }

    public void unregisterCallback(ServiceCallback cb) {
        callbacks_.remove(cb);
    }

    public void startControlVolume(int volume, int amplitude) {
        standard_volume_ = volume;
        standard_amplitude_ = amplitude;
        is_started_ = true;
    }

    public void stopControlVolume() {
        is_started_ = false;
    }

    public boolean isStartedAutoVolume() {
        return is_started_;
    }

    public int getAmplitude() {
        return decibel_meter_.getAmplitude();
    }

    public int getDecibel() {
        return decibel_meter_.getDecibel();
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

