package com.soky.dvol.control;

import android.widget.Toast;

import com.soky.dvol.MainActivity;
import com.soky.dvol.MainApplication;
import com.soky.dvol.R;

/**
 * 사용 편의를 위한 singleton 클래스
 */
public class Controller {
    public final String TAG = this.getClass().getSimpleName();

    private static Controller sInstance = new Controller();
    private Controller() {}

    private MainApplication mApp = null;
    private MainActivity mActivity = null;
    private ServiceController mSvc = new ServiceController();

    public static void start(MainActivity activity) {
        sInstance.mActivity = activity;
        sInstance.mApp = (MainApplication) activity.getApplication();
        sInstance.mSvc.start();
    }

    public static MainApplication getApplication() {
        return sInstance.mApp;
    }

    public static ServiceController getServiceController() {
        return sInstance.mSvc;
    }

    /**
     * 자동 볼륨 조절 시작/중지 시에 사용
     */
    public static void switchAutoVolume() {

        if (!sInstance.mSvc.isRunning()) {
            Toast.makeText(sInstance.mActivity, sInstance.mActivity.getResources().getString(R.string.wait_for_service), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isStarted = sInstance.mSvc.isStartedAutoVolume();
        if (isStarted) {
            sInstance.mActivity.stopAutoVolume();
        } else {
            sInstance.mActivity.startAutoVolume();
        }
    }

    /**
     * 앱 종료시 호출하는 메소드
     */
    public static void exit() {
        sInstance.mActivity.exit();
        sInstance.mSvc.stop();
    }
}
