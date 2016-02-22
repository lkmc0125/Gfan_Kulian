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
package com.xiaohong.kulian.common.vo;


/**
 * 
 * 下载状态更新信息
 * 
 * @author Andrew
 * @date    2011-6-2
 *
 */
public class DownloadInfo {
    /** DB id*/
    public long id;
    /** 应用名*/
    public String mAppName;
    /** 包名 */
    public String mPackageName;
    /** 下载状态 */
    public int mStatus;
    /** 下载进度(字符串带%) */
    public String mProgress;
    /** 下载进度(数字) */
    public int mProgressNumber;
    /** 总大小 */
    public long mTotalSize;
    /** 已下载大小 */
    public long mCurrentSize;
    /** 下载进度（动画） */
    public int mProgressLevel;
    /** 下载完成之后的文件存储路径 */
    public String mFilePath;
    /** ICON URL*/
    public Object mIconUrl;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "packagename : " + mPackageName + " status " + mStatus + " progress " + mProgress
                + " level " + mProgressLevel;
    }
}