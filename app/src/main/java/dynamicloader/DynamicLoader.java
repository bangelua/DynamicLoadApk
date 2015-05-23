package dynamicloader;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by luliang on 5/23/15.
 */
public class DynamicLoader {

    private static final String TAG = DynamicLoader.class.getSimpleName();
    private static final String DEXPATH = "dex_path";
    private static final String ODEX_PATH = "odex_path";

    private String assetApkFilename;
    private DexClassLoader mClassLoader;
    private Context mContxt;
    private String mApkName; //asset中存放的apk文件名，e.g. mumu.apk
    private String mApplicationName; //将要加载的apk的application名字
    private Application mApplication;


    public DynamicLoader(Context ctx, Application hostApplication, String apkName, String applicationName) {
        Log.d(TAG, "DynamicLoader init");
        mContxt = ctx;
        mApplication = hostApplication;
        if (TextUtils.isEmpty(applicationName)) {
            mApplicationName = Application.class.getName(); //默认
        } else {
            mApplicationName = applicationName;
        }
        try {
            init(apkName);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "e:  " + e);

        }

    }

    private void init(String apkName) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        mApkName = apkName;
        initDexFile();
        initClassLoader();
        replaceFileds();

    }

    private void replaceFileds() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Object mPackageInfo = ReflectionUtils.getFieldObject(mContxt, "mPackageInfo");
        ReflectionUtils.setField(mPackageInfo, "mClassLoader", mClassLoader);
        String resPath = new File(mContxt.getDir(DEXPATH, Context.MODE_PRIVATE), mApkName).getAbsolutePath();
        ReflectionUtils.setField(mPackageInfo, "mResDir", resPath);
        ReflectionUtils.setField(mPackageInfo, "mResources", null);

        Object mActivityThread = ReflectionUtils.getFieldObject(mPackageInfo, "mActivityThread");
        Method getResouces = mPackageInfo.getClass().getDeclaredMethod("getResources", mActivityThread.getClass());
        Object res = getResouces.invoke(mPackageInfo, mActivityThread);
        ReflectionUtils.setField(mApplication.getBaseContext(), "mResources", res);

        //application
        Application appl = (Application) Class.forName(mApplicationName, true, mContxt.getClassLoader()).newInstance();
        ReflectionUtils.setField(mPackageInfo, "mApplication", appl);
        ReflectionUtils.setField(mApplication.getBaseContext(), "mOuterContext", appl);
        ReflectionUtils.setField(mActivityThread, "mInitialApplication", appl);

        ArrayList<Application> appList = (ArrayList<Application>) ReflectionUtils.getFieldObject(mActivityThread, "mAllApplications");
        for (Application app : appList) {
            if (app == mApplication)
                app = appl;
        }


        Method attachMethod = Application.class.getDeclaredMethod("attach", Context.class);
        attachMethod.setAccessible(true);
        attachMethod.invoke(appl, mApplication.getBaseContext());
        appl.onCreate();

    }

    private void initDexFile() throws IOException {
        copyFile();
    }

    private void initClassLoader() {
        String dexPaths = null;
        File dexPath = mContxt.getDir(DEXPATH, Context.MODE_PRIVATE);
        File[] fileArray = dexPath.listFiles();
        for (File apk : fileArray) {
            if (TextUtils.isEmpty(dexPaths)) {
                dexPaths = apk.getAbsolutePath();
            } else {
                dexPaths += File.pathSeparator + apk.getAbsolutePath();
            }

        }
        Log.d(TAG, "dexPaths: " + dexPaths);

        File odexPath = mContxt.getDir(ODEX_PATH, Context.MODE_PRIVATE);
        mClassLoader = new DexClassLoader(dexPaths, odexPath.getAbsolutePath(), null, mContxt.getClassLoader());
        Log.d(TAG, "mClassLoader: " + mClassLoader);

    }

    /*
     *  copy asset file to dex path
     */
    private void copyFile() throws IOException {
        InputStream input = mContxt.getAssets().open(mApkName);
        File dexPath = mContxt.getDir(DEXPATH, Context.MODE_PRIVATE);
        if (!dexPath.exists()) {
            dexPath.mkdirs();
        }
        File outFile = new File(dexPath, mApkName);
        FileOutputStream out = new FileOutputStream(outFile);
        byte[] bytes = new byte[1024 * 5];
        int len;
        while ((len = input.read(bytes)) != -1) {
            out.write(bytes, 0, len);
        }
        out.close();
        input.close();
        Log.d(TAG, "FILE COPY FINISHED : " + outFile.getAbsolutePath());
    }

}
