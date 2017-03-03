package com.wujie.tinkerlearn.crash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.tencent.tinker.lib.tinker.TinkerApplicationHelper;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.wujie.tinkerlearn.reporter.SampleTinkerReport;
import com.wujie.tinkerlearn.util.TinkerManager;
import com.wujie.tinkerlearn.util.Utils;

/**
 * Created by wujie on 2017/3/3.
 */
public class SampleUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

    private static final String TAG = "Tinker.SampleUncaughtExHandler";

    private final Thread.UncaughtExceptionHandler ueh;
    public static final long QUICK_CRASH_ELAPSE = 10 * 1000;
    public static final int MAX_CRASH_COUNT = 3;
    public static final String DALVIX_XPOSED_CRASH = "Class ref in pre-verified class resolved to unexpected implementation";


    public SampleUncaughtExceptionHandler() {
        ueh = Thread.getDefaultUncaughtExceptionHandler();
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        TinkerLog.e(TAG, "uncaughtException:" + e.getMessage());
        tinkerFastCrashProtect();
        tinkerPreVerfiedCrashHandler(e);
        ueh.uncaughtException(t, e);
    }

    private void tinkerPreVerfiedCrashHandler(Throwable e) {
    Throwable throwable = e;
        boolean isXposed = false;
        while (throwable != null) {
            if (!isXposed) {
                isXposed =Utils.isXposedExists(e);
            }

            if (isXposed) {
                ApplicationLike applicationLike = TinkerManager.getTinkerApplicationLike();
                if (applicationLike == null || applicationLike.getApplication() == null) {
                    return;
                }

                if (!TinkerApplicationHelper.isTinkerLoadSuccess(applicationLike)) {
                    return;
                }

                boolean isCausedByXposed = false;

                if (throwable instanceof  IllegalAccessError && throwable.getMessage().contains(DALVIX_XPOSED_CRASH)) {
                    isCausedByXposed = true;
                }

                if (isCausedByXposed) {
                    SampleTinkerReport.onXposedCrash();
                    TinkerLog.e(TAG, "have xposed: just clean tinker");
                    //kill all other process to ensure that all process's code is the same.
                    ShareTinkerInternals.killAllOtherProcess(applicationLike.getApplication());

                    TinkerApplicationHelper.cleanPatch(applicationLike);
                    ShareTinkerInternals.setTinkerDisableWithSharedPreferences(applicationLike.getApplication());
                    return;
                }
            }
            throwable = throwable.getCause();
        }


    }

    /**
     * if tinker is load, and it crash more than MAX_CRASH_COUNT, then we just clean patch.
     */
    private boolean tinkerFastCrashProtect() {
        ApplicationLike applicationLike = TinkerManager.getTinkerApplicationLike();

        if (applicationLike == null || applicationLike.getApplication() == null) {
            return false;
        }
        if (!TinkerApplicationHelper.isTinkerLoadSuccess(applicationLike)) {
            return false;
        }

        final long elapsedTime = SystemClock.elapsedRealtime() - applicationLike.getApplicationStartElapsedTime();
        //this process may not install tinker, so we use TinkerApplicationHelper api
        if (elapsedTime < QUICK_CRASH_ELAPSE) {
            String currentVersion = TinkerApplicationHelper.getCurrentVersion(applicationLike);
            if (ShareTinkerInternals.isNullOrNil(currentVersion)) {
                return false;
            }

            SharedPreferences sp = applicationLike.getApplication().getSharedPreferences(ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS);
            int fastCrashCount = sp.getInt(currentVersion, 0) + 1;
            if (fastCrashCount >= MAX_CRASH_COUNT) {
                SampleTinkerReport.onFastCrashProtect();
                TinkerApplicationHelper.cleanPatch(applicationLike);
                TinkerLog.e(TAG, "tinker has fast crash more than %d, we just clean patch!", fastCrashCount);
                return true;
            } else {
                sp.edit().putInt(currentVersion, fastCrashCount).commit();
                TinkerLog.e(TAG, "tinker has fast crash %d times", fastCrashCount);
            }
        }

        return false;
    }
}
