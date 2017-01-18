package com.soky.dvol.control;

import android.widget.Toast;

import com.soky.dvol.MainActivity;
import com.soky.dvol.MainApplication;
import com.soky.dvol.R;

public class Controller {
    /**
     * singleton
     */
    private static Controller inst_ = new Controller();
    private Controller() {}

    private MainApplication app_ = null;
    private MainActivity activity_ = null;
    private ServiceController svc_ = new ServiceController();

    public static void start(MainActivity activity) {
        inst_.activity_ = activity;
        inst_.app_ = (MainApplication) activity.getApplication();
        inst_.svc_.start();
    }

    public static MainApplication getApplication() {
        return inst_.app_;
    }

    public static ServiceController getServiceController() {
        return inst_.svc_;
    }

    public static void switchAutoVolume() {

        if (!inst_.svc_.isRunning()) {
            Toast.makeText(inst_.activity_, inst_.activity_.getResources().getString(R.string.wait_for_service), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean started = inst_.svc_.isStartedAutoVolume();
        if (started) {
            inst_.activity_.stopAutoVolume();
        } else {
            inst_.activity_.startAutoVolume();
        }
    }

    /**
     * 앱 종료
     */
    public static void exit() {
        inst_.activity_.exit();
        inst_.svc_.stop();
    }
}
