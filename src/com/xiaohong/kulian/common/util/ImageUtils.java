/*
 * Copyright (C) 2016 Shanghai Xiaohong.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiaohong.kulian.common.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.AndroidHttpClient;
import com.xiaohong.kulian.common.HttpClientFactory;
import com.xiaohong.kulian.common.widget.LoadingDrawable;

/**
 * 本类提供关于图片下载的一些工具方法
 * 
 * @author andrew
 * @date 2011-3-21
 * 
 */
public class ImageUtils {

    public static Bitmap getImageFromUrl(String url) {

//        SessionManager cache = SessionManager.getInstance();

        HttpGet httpRequest = new HttpGet(url);
        HttpResponse response = null;
        AndroidHttpClient client = HttpClientFactory.get().getHttpClient(); 
        try {
            response = client.execute(httpRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from "
                        + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    // non-compressed images no need to be cached to the sd-card
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inPurgeable = true;
                    Bitmap bmp = BitmapFactory.decodeStream(new FlushedInputStream(inputStream),
                            null, o);
//                    cache.saveToDisk(url, bmp);
                    return bmp;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (ClientProtocolException e) {
            Log.w("ImageDownloader", "ClientProtocolException " + url);
        } catch (IOException e) {
            Log.w("ImageDownloader", "IOException " + url);
        } catch (Exception e) {
            Log.w("ImageDownloader", "other exception when download images from " + url);
        } catch (OutOfMemoryError err) {
            Log.w("ImageDownloader", "OutOfMemoryError when download images from " + url);
        } finally {
            httpRequest.abort();
        }
        return null;
    }
    
    
    private static final int TYPE_NORAML = 1;
    private static final int TYPE_IMAGE = 2;
    private static final int TYPE_SCREENSHOT = 3;
    private static final int TYPE_TOP = 4;
    static class BitmapDownloaderTask extends AsyncTask<Object, Void, Bitmap> {
        private Context context;
        private String url;
        private final WeakReference<ImageView> imageViewReference;
        private int type;
        CacheManager cache = CacheManager.getInstance();

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(Object... params) {
            // params comes from the execute() call: params[0] is the url.
            context = (Context) params[0];
            url = (String) params[1];
            type = (Integer) params[2];

            Bitmap bmp = getImageFromUrl(url);
            if (bmp != null && type != TYPE_IMAGE && type != TYPE_SCREENSHOT) {
                cache.cacheDrawable(url, bmp);
            }
            return bmp;
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = null;
                if (type == TYPE_IMAGE) {
                    bitmapDownloaderTask = getBitmapDownloaderTask2(imageView);
                } else {
                    bitmapDownloaderTask = getBitmapDownloaderTask1(imageView);
                }

                // Change bitmap only if this process is still associated with
                // it
                if (this == bitmapDownloaderTask && bitmap != null) {
                    if (type == TYPE_IMAGE) {
                        imageView.setBackgroundDrawable(null);
                        imageView.setImageBitmap(rotateImage(bitmap));
                    } else if(type == TYPE_SCREENSHOT) {
                        imageView.setImageBitmap(rotateImage(bitmap));
                    } else if (type == TYPE_TOP) {
                        BitmapDrawable background = new BitmapDrawable(bitmap);
                        StateListDrawable foreground = getMaskDrawable(context);
                        imageView.setImageDrawable(foreground);
                        imageView.setBackgroundDrawable(background);
                    } else {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }

    static class DownloadedDrawable1 extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference1;

        public DownloadedDrawable1(Drawable defaultBitmap, BitmapDownloaderTask bitmapDownloaderTask) {
            super(((BitmapDrawable) defaultBitmap).getBitmap());
            bitmapDownloaderTaskReference1 = new WeakReference<BitmapDownloaderTask>(
                    bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference1.get();
        }
    }

    static class DownloadedDrawable2 extends AnimationDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference2;

        public DownloadedDrawable2(Drawable defaultAnimation,
                BitmapDownloaderTask bitmapDownloaderTask) {
            AnimationDrawable drawable = ((AnimationDrawable) defaultAnimation);
            final int frameCounter = drawable.getNumberOfFrames();
            for (int i = 0; i < frameCounter; i++) {
                super.addFrame(drawable.getFrame(i), drawable.getDuration(i));
            }
            super.setOneShot(false);
            bitmapDownloaderTaskReference2 = new WeakReference<BitmapDownloaderTask>(
                    bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference2.get();
        }
    }

    // fetch normal task
    private static BitmapDownloaderTask getBitmapDownloaderTask1(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable == null || !(drawable instanceof DownloadedDrawable1)) {
                return null;
            }
            DownloadedDrawable1 downloadedDrawable = (DownloadedDrawable1) drawable;
            return downloadedDrawable.getBitmapDownloaderTask();
        }
        return null;
    }

    // fetch animation task
    private static BitmapDownloaderTask getBitmapDownloaderTask2(ImageView imageView) {
        if (imageView != null) {
             Drawable drawable = imageView.getBackground();
            if (drawable == null || !(drawable instanceof DownloadedDrawable2)) {
                return null;
            }
            DownloadedDrawable2 downloadedDrawable = (DownloadedDrawable2) drawable;
            return downloadedDrawable.getBitmapDownloaderTask();
        }
        return null;
    }

    /**
     * 下载产品列表的ICON
     */
    public static void download(Context context, String url, ImageView imageView) {

        CacheManager cache = CacheManager.getInstance();
        if (cache.existsDrawable(url)) {
            imageView.setImageBitmap(cache.getDrawableFromCache(url));
            return;
        }

        Drawable defaultDrawable = context.getResources().getDrawable(R.drawable.loading_icon);
        if (cancelPotentialBitmapDownload(url, imageView)) {
            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
            DownloadedDrawable1 downloadedDrawable = new DownloadedDrawable1(defaultDrawable, task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(context, url, TYPE_NORAML);
        }
    }

    /**
     * 下载产品详细页的截图
     */
    public static void downloadDeatilScreenshot(Context context, String url, ImageView imageView) {

        Drawable defaultDrawable = new BitmapDrawable();
        if (cancelPotentialBitmapDownload(url, imageView)) {
            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
            DownloadedDrawable1 downloadedDrawable = new DownloadedDrawable1(defaultDrawable, task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(context, url, TYPE_SCREENSHOT);
        }
    }
    
    /**
     * 下载截图页的截图
     */
    public static void downloadScreenShot(Context context, String url, ImageView imageView) {

        AnimationDrawable loadingDrawable = new LoadingDrawable(context);
        if (cancelPotentialImageDownload(url, imageView)) {
            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);

            DownloadedDrawable2 downloadedDrawable = new DownloadedDrawable2(loadingDrawable, task);
            // clear the old image
            imageView.setImageDrawable(null);
            imageView.setBackgroundDrawable(downloadedDrawable);
            downloadedDrawable.start();
            task.execute(context, url, TYPE_IMAGE);
        }
    }

    private static boolean cancelPotentialBitmapDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask1(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    private static boolean cancelPotentialImageDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask2(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获得首页顶部推荐的图（按下效果） 
     */
    private static StateListDrawable getMaskDrawable(Context context) {
        StateListDrawable stateDrawable = new StateListDrawable();
        int statePressed = android.R.attr.state_pressed;
        final Resources res = context.getResources();
        stateDrawable.addState(
                new int[] { statePressed },
                new BitmapDrawable(res, BitmapFactory
                        .decodeResource(res, R.drawable.banner_pressed)));
        return stateDrawable;
    }
    
    /**
     * 通过图片长宽比判断是否需要旋转处理
     */
    public static Bitmap rotateImage(Bitmap bmp) {

        if (bmp == null)
            return bmp;

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float aspectRatio = ((float) height) / width;

        if (aspectRatio > 1) {
            // no need to rotate the image
            return bmp;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = null;
        try {
            rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);
        } catch (OutOfMemoryError e) {
        }
        return rotatedBitmap;
    }
    
    /**
     * 获取适应屏幕大小的图 
     */
    public static Bitmap sacleBitmap(Context context, Bitmap bitmap) {
        // 适配屏幕大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        float aspectRatio = (float) screenWidth / (float) width;
        int scaledHeight = (int) (height * aspectRatio);
        Bitmap scaledBitmap = null;
        try {
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, scaledHeight, false);
        } catch (OutOfMemoryError e) {
        }
        return scaledBitmap;
    }
}

class FlushedInputStream extends FilterInputStream {
    public FlushedInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public long skip(long n) throws IOException {
        long totalBytesSkipped = 0L;
        while (totalBytesSkipped < n) {
            long bytesSkipped = in.skip(n - totalBytesSkipped);
            if (bytesSkipped == 0L) {
                int byteRead = read();
                if (byteRead < 0) {
                    break; // we reached EOF
                } else {
                    bytesSkipped = 1; // we read one byte
                }
            }

            totalBytesSkipped += bytesSkipped;
        }
        return totalBytesSkipped;
    }
}
