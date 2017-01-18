package com.soky.dvol.util;

import android.widget.Toast;

import com.soky.dvol.R;
import com.soky.dvol.control.Controller;

/**
 * 앱 종료 - 뒤로가기 두번 누를 경우
 */
public class BackButtonExiter {
    public static final long WAITING_TIME = 2000;
    public long pressed_time_ = 0;

    public void exit(android.app.Activity activity) {
        long now = System.currentTimeMillis();

        if (now <= (pressed_time_ + WAITING_TIME)){
            // 2초 안에 두번째 뒤로가기 누른 경우 앱 종료
            Controller.exit();
        } else {
            pressed_time_ = now;
            Toast.makeText(activity, activity.getResources().getString(R.string.exit_message), Toast.LENGTH_SHORT).show();
        }
    }
}
