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
    private AutoVolumeService service_ = null;
    private HashSet<AutoVolumeService.ServiceCallback> wait_callbacks_ =  new HashSet<AutoVolumeService.ServiceCallback>();

    public boolean isRunning() {
        return (service_ != null);
    }

    public boolean isStartedAutoVolume() {
        if (!isRunning()) return false;
        return service_.isStartedAutoVolume();
    }

    public AutoVolumeService getService() {
        if (service_ == null) {
            throw new RuntimeException("service is not running @@@@@@@@@@");
        }
        return service_;
    }

    public void start() {
        if (service_ != null) return;

        MainApplication app = Controller.getApplication();
        Intent intent = new Intent(app.getApplicationContext(), AutoVolumeService.class);
        app.bindService(intent, service_connection_, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        if (service_ == null) return;

        MainApplication app = Controller.getApplication();
        app.unbindService(service_connection_);
        service_ = null;
    }

    public void registCallback(AutoVolumeService.ServiceCallback cb) {
        if (isRunning()) {
            service_.registerCallback(cb);
        } else {
            wait_callbacks_.add(cb);
        }
    }

    public void unregistCallback(AutoVolumeService.ServiceCallback cb) {
        if (isRunning()) {
            service_.unregisterCallback(cb);
        }
    }

    private ServiceConnection service_connection_ = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AutoVolumeService.LocalBinder binder = (AutoVolumeService.LocalBinder)service;
            service_ = binder.getService();
            for (AutoVolumeService.ServiceCallback cb : wait_callbacks_) {
                service_.registerCallback(cb);
            }
            wait_callbacks_.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0){
            service_ = null;
        }
    };

}
