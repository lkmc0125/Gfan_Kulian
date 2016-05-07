package com.xiaohong.kulian.common.util;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池 异步加载图片示例（仅供参考，实际使用时，建议开发者自行处理图片加载部分）
 */
public class BitmapLoaderManager {
    public static ExecutorService executorService;

    private static ExecutorService Execotor() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                    .availableProcessors());
        }
        return executorService;
    }

    public static void loadBitmap(final Context context,
            final BitmapDownloadListener listener, final String[] bmUrl) {
        if (bmUrl != null) {
            for (String aBmUrl : bmUrl) {
                Execotor().execute(
                        new BitmapLoaderRunnable(context, listener, aBmUrl));
            }
        }
    }
}
