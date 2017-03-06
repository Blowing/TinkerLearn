package com.wujie.tinkerlearn.util;

import android.os.Environment;
import android.os.StatFs;

import com.tencent.tinker.loader.shareutil.ShareConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by wujie on 2017/3/3.
 *
 */

public class Utils {

    public static final int ERROR_PATCH_GOOGLEPLAY_CHANNEL = -5;
    public static final int ERROR_PATCH_ROM_SPACE = -6;
    public static final int ERROR_PATCH_MEMORY_LIMIT = -7;
    public static final int ERROR_PATCH_ALREADY_APPLY = -8;
    public static final int ERROR_PATCH_CRASH_LIMIT = -9;
    public static final int ERROR_PATCH_RETRY_COUNT_LIMIT = -10;
    public static final int ERROR_PATCH_CONDITION_NOT_SATISFIED = -11;

    public static final String PLATFORM = "platform";

    public static final int MIN_MEMORY_HEAP_SIZE = 45;

    private static boolean background = false;

    public static boolean isGooglePlay() {
        return false;
    }

    public static boolean isBackground() {
        return background;
    }

    public static void setBackground(boolean back) {
        background = back;
    }
    public static int checkForPatchRecover(long roomSize, int maxMemory) {
        if (Utils.isGooglePlay()) {
            return Utils.ERROR_PATCH_GOOGLEPLAY_CHANNEL;
        }
        if (maxMemory < MIN_MEMORY_HEAP_SIZE) {
            return Utils.ERROR_PATCH_MEMORY_LIMIT;
        }
        
        if (!checkRomSpaceEnough(roomSize)) {
            return Utils.ERROR_PATCH_ROM_SPACE;
        }
        return ShareConstants.ERROR_PATCH_OK;
    }

    public static boolean isXposedExists(Throwable thr) {
        StackTraceElement[] stackTraces = thr.getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {

            final String clazzName = stackTrace.getClassName();
            if (clazzName != null && clazzName.contains("de.robv.android.xposed.XposedBridge")) {
                return true;
            }
        }
        return false;

    }

    private static boolean checkRomSpaceEnough(long limitSize) {
        long allSize;
        long availableSize = 0;
        try{
            File data = Environment.getDataDirectory();
            StatFs sf = new StatFs(data.getPath());
            availableSize = sf.getAvailableBlocks() * sf.getBlockSize();
            allSize = sf.getBlockCount() * sf.getBlockSize();
        } catch (Exception e) {
            allSize = 0;
        }

        if (allSize != 0 && availableSize > limitSize) {
            return true;
        }
        return  false;
    }

    public static String getExceptionCauseString(final Throwable ex) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(bos);

        try {
            Throwable t = ex;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            t.printStackTrace(ps);
            return toVisualString(bos.toString());
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private static String toVisualString(String src) {
        boolean cutFlg = false;

        if (null == src) {
            return null;
        }
        char[] chr = src.toCharArray();
        if (null == chr) {
            return null;
        }

        int i = 0;
        for (; i < chr.length; i++) {
            if (chr[i] > 127) {
                chr[i] = 0;
                cutFlg = true;
                break;
            }
        }

        if (cutFlg) {
            return new String(chr, 0, i);
        } else {
            return src;
        }

    }

}
