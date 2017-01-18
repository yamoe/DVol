package com.soky.dvol.control;

import android.content.Context;
import android.content.SharedPreferences;

/**
 *
 */

public class Config {
    private static final int DEFAULT_DECIBEL = 35;
    private static final int DEFAULT_VOLUME = 6;

    private static final String FILE_NAME = "DVol";
    private static final String DECIBEL = "decibel";
    private static final String VOLUME = "volume";
    private static final String USE_NOW = "use_now";

    private static SharedPreferences pref() {
        return Controller.getApplication().getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static int getDecibel() {
        int decibel = pref().getInt(DECIBEL, -1);
        if (decibel != -1) return decibel;
        return DEFAULT_DECIBEL;
    }

    public static void setDecibel(int v) {
        SharedPreferences.Editor editor = pref().edit();
        editor.putInt(DECIBEL, v);
        editor.commit();
    }

    public static int getVolume() {
        int volume = pref().getInt(VOLUME, -1);
        if (volume != -1) return volume;
        return DEFAULT_VOLUME;
    }

    public static void setVolume(int v) {
        SharedPreferences.Editor editor = pref().edit();
        editor.putInt(VOLUME, v);
        editor.commit();
    }

    public static boolean getUseNow() {
        return pref().getBoolean(USE_NOW, false);
    }

    public static void setUseNow(boolean v) {
        SharedPreferences.Editor editor = pref().edit();
        editor.putBoolean(USE_NOW, v);
        editor.commit();
    }

}

