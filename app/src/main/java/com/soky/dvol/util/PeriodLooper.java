package com.soky.dvol.util;

import android.os.Handler;

public class PeriodLooper {

    private final Handler handler_ = new Handler();
    private boolean is_running_ = false;
    private int period_msec_ = 0;

    private Runnable user_loop_;
    private Runnable main_loop_ = new Runnable() {
        @Override
        public void run() {
            if (!is_running_) {
                return;
            }

            user_loop_.run();

            if (is_running_) {
                post();
            }
        }
    };

    public void start(Runnable user_loop, int period_msec) {
        user_loop_ = user_loop;
        period_msec_ = period_msec;

        if (!is_running_) {
            is_running_ = true;
            post();
        }
    }

    public void stop() {
        if (is_running_) {
            is_running_ = false;
        }
    }

    private void post() {
        handler_.postDelayed(main_loop_, period_msec_);
    }


}
