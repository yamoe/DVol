package com.soky.dvol.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 *
 */

public class ProjectInfo {
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageInfo i  = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageInfo i  = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = i.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

}
