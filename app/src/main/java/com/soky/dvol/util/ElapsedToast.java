package com.soky.dvol.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 특정 시간안에는 반복하여 메시지를 띄우지 않기 위함
 */

public class ElapsedToast {
    private Toast toast_;
    private int msec_;
    private long previous_toasted_time_ = 0;

    public ElapsedToast(Context c, int id) {
        this(c, id, 2000);
    }

    public ElapsedToast(Context c, int id, int msec) {
        toast_ = Toast.makeText(c, c.getResources().getString(id), Toast.LENGTH_SHORT);
    }

    public void show() {
        long now = System.currentTimeMillis();
        if ((now - previous_toasted_time_) > msec_) {
            previous_toasted_time_ = now;
            toast_.show();
        }
    }
}
