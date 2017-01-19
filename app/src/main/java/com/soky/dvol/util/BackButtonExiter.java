package com.soky.dvol.util;

import android.content.Context;
import android.widget.Toast;

import com.soky.dvol.R;
import com.soky.dvol.control.Controller;

/**
 * 앱 종료 - 뒤로가기 두번 누를 경우
 */
public class BackButtonExiter {
    private static final long WAITING_TIME = 2000;

    private long mPreviousPressedTime = 0;
    private Toast mToast;

    public void exit(Context context) {
        long now = System.currentTimeMillis();

        if (now <= (mPreviousPressedTime + WAITING_TIME)){
            // 2초 안에 두번째 뒤로가기 누른 경우 앱 종료
            if (mToast != null) {
                mToast.cancel();
            }
            Controller.exit();
        } else {
            mPreviousPressedTime = now;
            mToast = Toast.makeText(context, context.getResources().getString(R.string.exit_message), Toast.LENGTH_SHORT);
            mToast.show();
        }
    }
}
