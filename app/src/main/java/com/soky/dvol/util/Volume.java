package com.soky.dvol.util;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import com.soky.dvol.control.Controller;

/**
 * 볼륨 컨트롤 관련 클래스
 */
public class Volume {
    public final String TAG = this.getClass().getSimpleName();

    private AudioManager mAudioManager;
    private SettingsContentObserver mObserver;

    private int mMaxVolume = 15;

    public Volume() {
        Context context = Controller.getApplication().getApplicationContext();
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public int getMax() {
        return mMaxVolume;
    }

    public int getCurrent() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void setCurrent(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
    }

    private static class SettingsContentObserver extends ContentObserver {
        private Volume mVolume;
        private Listener mListener;

        private SettingsContentObserver(Volume v, Listener l) {
            // 핸들 대신 null을 주면 다른 스레드로 onChange가 호출되서 UI 조작이 불편해 짐
            // registerListener() 를 호출한 스레드와 동일한 스레드로 콜백이 가게 하기 위해 핸들러 생성하여 전달
            super(new Handler());
            mVolume = v;
            mListener = l;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            mListener.onChange(mVolume.getCurrent());
        }
    }

    /**
     * 시스템의 음악 볼륨이 변경되는 경우 콜백 받기 위한 함수
     * @param l    콜백 객체
     */
    public void registerListener(Listener l) {
        if (l == null) return;

        mObserver = new SettingsContentObserver(this, l);
        Context context = Controller.getApplication().getApplicationContext();
        context.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mObserver);
    }

    public void unregisterListener() {
        if (mObserver != null) {
            Context context = Controller.getApplication().getApplicationContext();
            context.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

    public interface Listener {
        void onChange(int volume);
    }
}
