package com.xiaohong.kulian.common.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.xiaohong.kulian.R;

/**
 * 线程池 异步加载图片示例（仅供参考，实际使用时，建议开发者自行处理图片加载部分）
 */
public class BitmapLoaderRunnable implements Runnable {

    private Context mContext;

    private String mBmUrl;

    private BitmapDownloadListener mListener;

    public BitmapLoaderRunnable(Context context,
            BitmapDownloadListener listener, String bmUrl) {
        this.mContext = context;
        this.mListener = listener;
        this.mBmUrl = bmUrl;
    }

    @Override
    public void run() {
        // 本地中没有图像，则从网络上取出数据，并将取出的数据缓存到内存中
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            if (mBmUrl != null) {
                String storeFileName = Utils.getMD5(mBmUrl);
                if (!TextUtils.isEmpty(storeFileName)) {
                    File file = mContext.getFileStreamPath(storeFileName);
                    if (file.exists()) {
                        // 如果本地已经有图片的话
                        mListener.onLoadBitmap(mBmUrl,
                                BitmapFactory.decodeFile(file.getPath()));
                    } else {
                        try {

                            // 如果本地没有图片就上网下载
                            HttpURLConnection conn = (HttpURLConnection) ((new URL(
                                    mBmUrl)).openConnection());
                            conn.setDoInput(true);
                            conn.connect();

                            if (conn.getResponseCode() != 200) {

                                // 网络加载失败,则显示加载失败的图片
                                mListener.onLoadBitmap(mBmUrl, BitmapFactory
                                        .decodeResource(
                                                mContext.getResources(),
                                                R.drawable.icon_default));
                                Log.e("Youmi", mBmUrl + " failed to load icon");
                            } else {
                                fos = new FileOutputStream(file);
                                int len = 0;
                                byte[] buff = new byte[1024];
                                is = conn.getInputStream();
                                while ((len = is.read(buff)) > 0) {
                                    fos.write(buff, 0, len);
                                }
                                fos.flush();
                                fos.close();
                                fos = null;
                                mListener.onLoadBitmap(mBmUrl, BitmapFactory
                                        .decodeFile(file.getPath()));
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

}
