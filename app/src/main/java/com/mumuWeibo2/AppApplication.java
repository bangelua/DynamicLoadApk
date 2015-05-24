package com.mumuWeibo2;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import dynamicloader.DynamicLoader;

/**
 * Created by luliang on 5/23/15.
 */
public class AppApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d("luliang", "base: " + base.getClass());
        String process = getProcessName(base);
        if (process != null && process.endsWith("weibo"))
            new DynamicLoader(base, this, "mumu.apk", "com.mumuWeibo2.MumuApplication");
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        new DynamicLoader(this, "mumu.apk");
    }

    public static String getProcessName(Context context) {
        String myProcessName = null;
        int i = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context
                .ACTIVITY_SERVICE);
        while (TextUtils.isEmpty(myProcessName)) {

            List<ActivityManager.RunningAppProcessInfo> list = activityManager
                    .getRunningAppProcesses();
            int j = list.size();
            int k = 0;
            while (k < j) {
                if (((ActivityManager.RunningAppProcessInfo) list.get(k)).pid == i) {
                    String str3 = ((ActivityManager.RunningAppProcessInfo) list
                            .get(k)).processName;
                    myProcessName = str3;
                }
                k += 1;
            }
        }
        return myProcessName;
    }
}
