package com.soky.dvol.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 특정 시간안에는 반복하여 메시지를 띄우지 않기 위함
 */

public class ElapsedToast {
    private int mMessageID;
    private int mMsec;
    private long mPreviousToastedTime = 0;

    public ElapsedToast(int id) {
        this(id, 2000);
    }

    private ElapsedToast(int id, int msec) {
        mMessageID = id;
        mMsec = msec;
    }

    public void show(Context context) {
        long now = System.currentTimeMillis();
        if ((now - mPreviousToastedTime) > mMsec) {
            mPreviousToastedTime = now;
            Toast.makeText(context, context.getResources().getString(mMessageID), Toast.LENGTH_SHORT).show();
        }
    }
}
