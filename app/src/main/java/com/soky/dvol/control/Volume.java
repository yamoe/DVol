package com.soky.dvol.control;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

/**
 * 볼륨 컨트롤 관련 클래스
 */

public class Volume {
    private AudioManager audio_manager_;
    private SettingsContentObserver observer_;

    private int max_volume_ = 15;

    public Volume() {
        Context context = Controller.getApplication().getApplicationContext();
        audio_manager_ = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        max_volume_ = audio_manager_.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public int getMax() {
        return max_volume_;
    }

    public int getCurrent() {
        return audio_manager_.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void setCurrent(int volumn) {
        audio_manager_.setStreamVolume(AudioManager.STREAM_MUSIC, volumn, AudioManager.FLAG_PLAY_SOUND);
    }

    private static class SettingsContentObserver extends ContentObserver {
        private Volume volume_;
        private Listener listener_;

        public SettingsContentObserver(Volume v, Listener l) {
            // 핸들 대신 null을 주면 다른 스레드로 onChange가 호출되서 UI 조작이 불편해 짐
            // registerListener() 를 호출한 스레드와 동일한 스레드로 콜백이 가게 하기 위해 핸들러 생성하여 전달
            super(new Handler());
            volume_ = v;
            listener_ = l;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            listener_.onChange(volume_.getCurrent());
        }
    }

    /**
     * 시스템의 음악 볼륨이 변경되는 경우 콜백 받기 위한 함수
     * @param l    콜백 객체
     */
    public void registerListener(Listener l) {
        if (l == null) return;

        observer_ = new SettingsContentObserver(this, l);
        Context context = Controller.getApplication().getApplicationContext();
        context.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, observer_);
    }

    public void unregisterListener() {
        if (observer_ != null) {
            Context context = Controller.getApplication().getApplicationContext();
            context.getContentResolver().unregisterContentObserver(observer_);
            observer_ = null;
        }
    }

    public interface Listener {
        void onChange(int volume);
    }
}
