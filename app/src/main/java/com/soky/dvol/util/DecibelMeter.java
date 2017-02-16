package com.soky.dvol.util;

import android.media.MediaRecorder;

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
public class DecibelMeter implements DecibelMeterInterface {
    private final String TAG = this.getClass().getSimpleName();

    public final static int MAX_DECIBEL = 120;  // 임의로 120을 MAX로 함
    public final static int MAX_AMPLITUDE = 32767;

    private MediaRecorder mRecorder = null;

    private int mDecibel = 0;
    private int mAmplitude = 0;

    @Override
    public void initialize() {
        if (mRecorder != null) return;

        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");

            mRecorder.prepare();
            mRecorder.start();

        } catch (Exception e) {
            // 다른 앱이 마이크 권한을 점유하고 있는 경우IOException, IllegalStateException 발생 가능
            // Log.d(TAG, e.getMessage());
            mRecorder = null;
        }
    }

    @Override
    public void uninitialize() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void measure() {
        mAmplitude = measureAmplitude();
        mDecibel = measureDecibel(mAmplitude);
    }

    @Override
    public int getDecibel() {
        return mDecibel;
    }

    @Override
    public int getAmplitude() {
        return mAmplitude;
    }

    public static int toAmplitude(int decibel) {
        return (int)Math.pow(10, (double)decibel/20);
    }

    private int measureAmplitude() {
        initialize();

        if (mRecorder == null) {
            return 0;
        }
        /* getMaxAmplitude() 설명
            MAX_AMPLITUDE : 최대값 32767 (90.308... 데시벨)
            호출간격 사이의 최대 진폭이므로 연속으로 호출되면 대부분 0 을 리턴함
         */
        return mRecorder.getMaxAmplitude();
    }

    private int measureDecibel(int amp) {
        int decibel = 0;
        //amp = amp / 1; //BASE 600, 300
        if (amp > 1) {
            decibel = (int)(20 * Math.log10(amp));
        }
        return decibel;
    }

}
