package com.soky.dvol.util;

import java.util.LinkedList;

/**
 * 평균 데시벨
 */

public class AverageDecibelMeter implements DecibelMeterInterface {
    private final String TAG = this.getClass().getSimpleName();
    private final DecibelMeter mDecibelMeter = new DecibelMeter();

    private int mSize = 0;
    private LinkedList<Data> mList = new LinkedList<>();
    private int mDecibelAverage;
    private int mAmplitudeAverage;

    class Data {
        public int mDecibel;
        public int mAmplitude;

        public Data(int decibel, int amplitude) {
            mDecibel = decibel;
            mAmplitude = amplitude;
        }
    }

    public AverageDecibelMeter(int size) {
        mSize = size;
    }

    @Override
    public void initialize() {
        mDecibelMeter.initialize();
    }

    @Override
    public void uninitialize() {
        mDecibelMeter.uninitialize();
    }

    @Override
    public void measure() {
        mDecibelMeter.measure();
        mList.add(new Data(mDecibelMeter.getDecibel(), mDecibelMeter.getAmplitude()));

        // mSize 로 유지하고 오래된것은 삭제
        for (int i = mList.size() - mSize; i > 0; --i) {
            mList.removeFirst();
        }

        // 평균 구하기
        mDecibelAverage = 0;
        mAmplitudeAverage = 0;
        for (Data data : mList) {
            mDecibelAverage += data.mDecibel;
            mAmplitudeAverage += data.mAmplitude;
        }
        mDecibelAverage /= mList.size();
        mAmplitudeAverage /= mList.size();
    }

    @Override
    public int getDecibel() {
        return mDecibelAverage;
    }

    @Override
    public int getAmplitude() {
        return mAmplitudeAverage;
    }

}
