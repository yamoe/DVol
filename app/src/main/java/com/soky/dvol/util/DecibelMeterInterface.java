package com.soky.dvol.util;

/**
 *
 */

public interface DecibelMeterInterface {
    public void initialize();
    public void uninitialize();
    public void measure();
    public int getDecibel();
    public int getAmplitude();
}
