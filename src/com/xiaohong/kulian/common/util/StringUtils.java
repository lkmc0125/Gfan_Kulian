/*
 * Copyright (C) 2010 mAPPn.Inc
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

import java.text.DecimalFormat;

import android.text.TextUtils;

/**
 * 字符串处理工具类
 * 
 * @author andrew
 * @date    2011-4-26
 * @since   Version 0.7.0
 */
public class StringUtils {

    /**
     * 从URI中获取文件名
     */
    public static String getFileNameFromUrl(final String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }
    
    /**
     * 格式化文件大小
     */
    public static String formatSize(long size) {
       if (size < 1048576L)
            return new DecimalFormat("##0").format((float) size / 1024f) + "K";
        else if (size < 1073741824L)
            return new DecimalFormat("###0.##").format((float) size / 1048576f) + "M";
        else
            return new DecimalFormat("#######0.##").format((float) size / 1073741824f) + "G";
    }
    
    /**
     * 格式化文件大小
     */
    public static String formatSize(String size) {
        return formatSize(Utils.getLong(size));
    }
    
    /**
     * 格式化下载量数据 
     */
    public static String getDownloadInterval(int downloadNum) {
        if (downloadNum < 50) {
            return "小于50";
        } else if (downloadNum >= 50 && downloadNum < 100) {
            return "50 - 100";
        } else if (downloadNum >= 100 && downloadNum < 500) {
            return "100 - 500";
        } else if (downloadNum >= 500 && downloadNum < 1000) {
            return "500 - 1,000";
        } else if (downloadNum >= 1000 && downloadNum < 5000) {
            return "1,000 - 5,000";
        } else if (downloadNum >= 5000 && downloadNum < 10000) {
            return "5,000 - 10,000";
        } else if (downloadNum >= 10000 && downloadNum < 50000) {
            return "10,000 - 50,000";
        } else if (downloadNum >= 50000 && downloadNum < 250000) {
            return "50,000 - 250,000";
        } else {
            return "大于250,000";
        }
    }
}
