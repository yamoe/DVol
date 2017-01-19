package com.soky.dvol.control;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.soky.dvol.AutoVolumeService;
import com.soky.dvol.MainApplication;

import java.util.HashSet;

public class ServiceController {
    private final String TAG = this.getClass().getSimpleName();
    private AutoVolumeService mService = null;
    private HashSet<AutoVolumeService.ServiceCallback> mWaitingCallbacks =  new HashSet<>();

    /**
     * 서비스 실행 여부
     * @return 서비스 실행 여부
     */
    boolean isRunning() {
        return (mService != null);
    }

    /**
     * 서비스의 자동 볼륨 조절 실행 여부
     * @return 서비스의 자동 볼륨 조절 실행 여부
     */
    public boolean isStartedAutoVolume() {
        return isRunning() && mService.isStartedAutoVolume();
    }

    public AutoVolumeService getService() {
        if (mService == null) {
            // 서비스가 실행된 후 호출될 수 있어야 함
            throw new RuntimeException("service is not running @@@@@@@@@@");
        }
        return mService;
    }

    void start() {
        if (mService != null) return;

        MainApplication app = Controller.getApplication();
        Intent intent = new Intent(app.getApplicationContext(), AutoVolumeService.class);
        app.bindService(intent, service_connection_, Context.BIND_AUTO_CREATE);
    }

    void stop() {
        if (mService == null) return;

        MainApplication app = Controller.getApplication();
        app.unbindService(service_connection_);
        mService = null;
    }

    public void registCallback(AutoVolumeService.ServiceCallback cb) {
        if (isRunning()) {
            mService.registerCallback(cb);
        } else {
            mWaitingCallbacks.add(cb);
        }
    }

    public void unregistCallback(AutoVolumeService.ServiceCallback cb) {
        if (isRunning()) {
            mService.unregisterCallback(cb);
        }
    }

    private ServiceConnection service_connection_ = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AutoVolumeService.LocalBinder binder = (AutoVolumeService.LocalBinder)service;
            mService = binder.getService();
            for (AutoVolumeService.ServiceCallback cb : mWaitingCallbacks) {
                mService.registerCallback(cb);
            }
            mWaitingCallbacks.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0){
            mService = null;
        }
    };

}
