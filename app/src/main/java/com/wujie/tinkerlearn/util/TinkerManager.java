package com.wujie.tinkerlearn.util;

import com.tencent.tinker.lib.listener.PatchListener;
import com.tencent.tinker.lib.patch.AbstractPatch;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.reporter.LoadReporter;
import com.tencent.tinker.lib.reporter.PatchReporter;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.wujie.tinkerlearn.crash.SampleUncaughtExceptionHandler;
import com.wujie.tinkerlearn.reporter.SampleLoadReporter;
import com.wujie.tinkerlearn.reporter.SamplePatchListener;
import com.wujie.tinkerlearn.reporter.SamplePatchReporter;
import com.wujie.tinkerlearn.service.SampleResultService;

/**
 * Created by wujie on 2017/3/3.
 */
public class TinkerManager {

    private static final String TAG = "Tinker.TinkerManager";


    private static ApplicationLike applicationLike;

    private static SampleUncaughtExceptionHandler uncaughtExceptionHandler;

    private static boolean isInstalled = false;

    public static void setTinkerApplicationLike(ApplicationLike appLike) {
        applicationLike = appLike;
    }

    public static  ApplicationLike getTinkerApplicationLike() {
        return  applicationLike;
    }

    public static void initFastCrashProtect() {
        if (uncaughtExceptionHandler == null) {
            uncaughtExceptionHandler = new SampleUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
    }

    public static void setUpgradeRetryEnable(boolean enable) {
        UpgradePatchRetry.getInstance(applicationLike.getApplication()).setRetryEnable(enable);
    }

    public static void sampleInstallTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }
        TinkerInstaller.install(appLike);
        isInstalled = true;
    }

    public static void installTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }

        LoadReporter loadReporter = new SampleLoadReporter(appLike.getApplication());
        PatchReporter patchReporter = new SamplePatchReporter(appLike.getApplication());
        PatchListener patchListener = new SamplePatchListener(appLike.getApplication());

        AbstractPatch upgradePatchProcessor = new UpgradePatch();

        TinkerInstaller.install(appLike, loadReporter, patchReporter, patchListener,
                SampleResultService.class, upgradePatchProcessor);

        isInstalled = true;
    }

}
