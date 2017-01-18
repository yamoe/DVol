package com.soky.dvol.control;

import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

/*
    dB		느낌					소리의 종류
    120 dB	견디기 어렵다			제트기 이륙(60 m 떨어진 곳에서)
    110 dB	견디기 어렵다			공사장 소음, 헤비메탈 연주회
    100 dB	대단히 시끄럽다			고함(1.5m 에서)
    90 dB	대단히 시끄럽다			대형 트럭(15m 에서), 굴착기(1m)
    80 dB	꽤 시끄럽다				대도시 거리 소음
    70 dB	꽤 시끄럽다				자동차 실내 소음
    60 dB	보통					보통 대화(1m 떨어져서)
    50 dB	보통					교실, 사무실
    40 dB	조용하다				조용한 거실
    30 dB	고요하다				밤중의 침실
    20 dB	방송국 스투디오			밤중의 침실
    10 dB	겨우 무엇인가 들린다	나뭇잎 스치는 소리
    0 dB	겨우 무엇인가 들린다	들을 수 있는 가장 작은 소리
 */
public class DecibelMeter {
    private final String TAG = this.getClass().getSimpleName();

    public final static int MAX_DECIBEL = 120;  // 임의로 120을 MAX로 함
    public final static int MAX_AMPLITUDE = 32767;

    private MediaRecorder recorder_ = null;
    private final Handler handler_ = new Handler();

    private int decibel_ = 0;
    private int amplitude_ = 0;

    public void init() {
        if (recorder_ != null) return;

        try {
            recorder_ = new MediaRecorder();
            recorder_.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder_.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder_.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder_.setOutputFile("/dev/null");

            recorder_.prepare();
            recorder_.start();

        } catch (Exception e) {
            // IOException, IllegalStateException 발생 가능
            Log.d(TAG, e.getMessage());
            recorder_ = null;
        }
    }

    public void uninit() {
        if (recorder_ != null) {
            recorder_.stop();
            recorder_.reset();
            recorder_.release();
            recorder_ = null;
        }
    }

    public void measure() {
        amplitude_ = measureAmplitude();
        decibel_ = measureDecibel(amplitude_);
    }

    public int getDecibel() {
        return decibel_;
    }

    public int getAmplitude() {
        return amplitude_;
    }

    public static int toAmplitude(int decibel) {
        return (int)Math.pow(10, (double)decibel/20);
    }

    private int measureAmplitude() {
        init();

        if (recorder_ == null) {
            return 0;
        }
        /* getMaxAmplitude() 설명
            MAX_AMPLITUDE : 최대값 32767 (90.308... 데시벨)
            호출간격 사이의 최대 진폭이므로 연속으로 호출되면 대부분 0 을 리턴함
         */
        return recorder_.getMaxAmplitude();
    }

    private int measureDecibel(int amp) {
        int decibel = 0;
        amp = amp / 1; //BASE 600, 300
        if (amp > 1) {
            decibel = (int)(20 * Math.log10(amp));
        }
        return decibel;
    }

}
