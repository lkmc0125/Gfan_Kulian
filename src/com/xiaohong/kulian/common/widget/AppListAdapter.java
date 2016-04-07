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
package com.xiaohong.kulian.common.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.Session;
import com.xiaohong.kulian.adapter.CommonAdapter;
import com.xiaohong.kulian.common.MarketAPI;
import com.xiaohong.kulian.common.ApiAsyncTask.ApiRequestListener;
import com.xiaohong.kulian.common.download.DownloadManager;
import com.xiaohong.kulian.common.download.DownloadManager.Request;
import com.xiaohong.kulian.common.util.ImageUtils;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.DownloadInfo;
import com.xiaohong.kulian.common.vo.DownloadItem;
import com.xiaohong.kulian.common.vo.UpgradeInfo;
import com.xiaohong.kulian.ui.RegisterActivity;


/**
 * GfanClient ListView associating adapter<br>
 * It has lazyload feature, which load data on-demand.
 * 
 * @author andrew.wang
 * 
 */
public class AppListAdapter extends CommonAdapter implements Observer, ApiRequestListener {

	private ArrayList<HashMap<String, Object>> mDataSource;
	private LazyloadListener mLazyloadListener;

	private int mResource;
	private int mDividerResource;
	private boolean mHasGroup;
	private boolean mIsLazyLoad;
	private String[] mFrom;
	private int[] mTo;
	private LayoutInflater mInflater;
	private Context mContext;
	private boolean mIsProductList;
	private HashMap<String, DownloadInfo> mDownloadingTask;
	private ArrayList<String> mInstalledList;
	private DownloadManager mDownloadManager;
	private HashMap<String, HashMap<String, Object>> mDownloadExtraInfo;
	private HashMap<String, String> mIconCache;
	private HashMap<String, HashMap<String, Object>> mCheckedList;
	private HashMap<String, UpgradeInfo> mUpdateList;
	private boolean mIsRankList;
	private String mPageType = Constants.GROUP_14;

    /**
	 * Application list adapter<br>
	 * 如果不希望这个子View显示，设置Key对应的Value为Null即可
	 * 
	 * @param context
	 *            application context
	 * @param data
	 *            the datasource behind the listview
	 * @param resource
	 *            list item view layout resource
	 * @param from
	 *            the keys array of data source which you want to bind to the
	 *            view
	 * @param to
	 *            array of according view id
	 * @param hasGroup
	 *            whether has place holder
	 */
    public AppListAdapter(Context context, ArrayList<HashMap<String, Object>> data, int resource,
            String[] from, int[] to) {
        if (data == null) {
            mDataSource = new ArrayList<HashMap<String, Object>>();
        } else {
            mDataSource = data;
        }
        mContext = context;
        mResource = resource;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCheckedList = new HashMap<String, HashMap<String, Object>>();
        mIconCache = new HashMap<String, String>();
    }

	/**
     * @return the mCheckedList
     */
    public HashMap<String, HashMap<String, Object>> getCheckedList() {
        return mCheckedList;
    }

    /**
	 * 设置是否包含分隔项
	 * 
	 * @param flag
	 *            默认是false, 不包含分隔符
	 */
	public void setContainsPlaceHolder(boolean flag) {
		mHasGroup = flag;
	}

	/**
	 * 设置分隔项的资源ID
	 */
	public void setPlaceHolderResource(int id) {
		mDividerResource = id;
	}
	
	/**
	 * 排行榜，需要识别排位
	 */
	public void setRankList() {
	    mIsRankList = true;
	}
	
    /**
     * 用于统计Lable
     */
    public void setmPageType(String mPageType) {
        this.mPageType = mPageType;
    }

	/**
	 * 产品列表，需要刷新产品状态
	 */
	public void setProductList() {
	    mIsProductList = true;
	    Session session = Session.get(mContext);
	    session.addObserver(this);
	    mDownloadManager = session.getDownloadManager();
	    mInstalledList = session.getInstalledApps();
	    mDownloadingTask = session.getDownloadingList();
	    mUpdateList = session.getUpdateList();
	    mDownloadExtraInfo = new HashMap<String, HashMap<String,Object>>();
	}
	
	/*
	 * How many items are in the data set represented by this Adapter.
	 */
	@Override
	public int getCount() {
		if (mDataSource == null) {
			return 0;
		}
		return mDataSource.size();
	}

	@Override
	public Object getItem(int position) {

		if (mDataSource != null && position < getCount()) {
			return mDataSource.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEmpty() {
		if (mDataSource == null || mDataSource.size() == 0) {
			return true;
		}
		return super.isEmpty();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	/*
	 * Clear all the data
	 */
	public void clearData() {
		if (mDataSource != null) {
			mDataSource.clear();
			notifyDataSetChanged();
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (mHasGroup && isPlaceHolder(position)) {
			// place holder for group
			return 1;
		}
		// normal item
		return 0;
	}

	/*
	 * Return the view types of the list adapter
	 */
	@Override
	public int getViewTypeCount() {
		if (mHasGroup) {
			return 2;
		} else {
			return 1;
		}
	}

	/**
	 * Lazyload web data
	 * 
	 * @param newData
	 */
	public void addData(ArrayList<HashMap<String, Object>> newData) {
		if (newData != null && newData.size() > 0) {
			mDataSource.addAll(getCount(), newData);
			notifyDataSetChanged();
		}
	}

	public void addData(HashMap<String, Object> newData) {
		if (newData != null) {
			mDataSource.add(getCount(), newData);
			notifyDataSetChanged();
		}
	}
	
    public void removeData(HashMap<String, Object> oldData) {
        if (mDataSource != null) {
            mDataSource.remove(oldData);
            notifyDataSetChanged();
        }
    }
    
    public void removeData(int position) {
        if (mDataSource != null) {
            mDataSource.remove(position);
            notifyDataSetChanged();
        }
    }

	public void insertData(HashMap<String, Object> newData) {
		if (newData != null) {
			mDataSource.add(0, newData);
			notifyDataSetChanged();
		}
	}

	public void setLazyloadListener(LazyloadListener listener) {
		mIsLazyLoad = true;
		mLazyloadListener = listener;
	}

	@Override
	public boolean isEnabled(int position) {
		if (mHasGroup) {
			return !isPlaceHolder(position);
		} else {
			return true;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// last 4 item trigger the lazyload event
		if (mIsLazyLoad && !mLazyloadListener.isEnd()
				&& (position == getCount() - 4)) {
			// fix the multi-load situation
			synchronized (this) {
				if (mLazyloadListener.isLoadOver()) {
					mLazyloadListener.lazyload();
                    Utils.trackEvent(mContext, mPageType, Constants.PRODUCT_LAZY_LOAD);
				}
			}
		}

		// reach here when list is not at the end
		assert (position < getCount());
		View v;
        if (convertView == null) {
            v = newView(position, parent);
        } else {
            v = convertView;
        }
        
        if (mIsProductList && mDownloadingTask != null) {
            
            // 列表中存在下载列表的任务，更新状态
            HashMap<String, Object> item = mDataSource.get(position);
            String packageName = (String) item.get(Constants.KEY_PRODUCT_PACKAGE_NAME);
            if (mDownloadingTask.containsKey(packageName)) {
                DownloadInfo info = mDownloadingTask.get(packageName);
                // 下载过程中，刷新进度
                item.put(Constants.KEY_PRODUCT_INFO, info.mProgress);
                item.put(Constants.KEY_PRODUCT_DOWNLOAD, info.mProgressLevel);
            } else if (mInstalledList.contains(packageName)) {
                // 已经安装的应用
                if (mUpdateList.containsKey(packageName)) {
                    // 可以更新
                    item.put(Constants.KEY_PRODUCT_DOWNLOAD, Constants.STATUS_UPDATE);
                } else {
                    item.put(Constants.KEY_PRODUCT_DOWNLOAD, Constants.STATUS_INSTALLED);
                }
            } else {
                Object result = item.get(Constants.KEY_PRODUCT_DOWNLOAD);
                if (result != null) {
                    int status = (Integer) result;
                    if (status == Constants.STATUS_PENDING) {
                        // 准备开始下载，无需处理
                    } else if (status != Constants.STATUS_DOWNLOADED) {
                        // 默认的状态是未安装
                        item.put(Constants.KEY_PRODUCT_DOWNLOAD, Constants.STATUS_NORMAL);
                    }
                }
            }
        }
		
		bindView(position, v);
		return v;
	}

	/*
	 * Create new view object and cache some views associated with it
	 */
	private View newView(int position, ViewGroup parent) {
		View v;
		if (mHasGroup && isPlaceHolder(position)) {
			v = mInflater.inflate(mDividerResource, parent, false);
		} else {
			v = mInflater.inflate(mResource, parent, false);
		}

		final int[] to = mTo;
		final int count = to.length;
		final View[] holder = new View[count];

        for (int i = 0; i < count; i++) {
            holder[i] = v.findViewById(to[i]);
        }

		v.setTag(holder);
		return v;
	}
	
	/*
	 * bind the background data to the view
	 */
	private void bindView(int position, View view) {

		final HashMap<String, Object> dataSet = mDataSource.get(position);
		if (dataSet == null) {
			return;
		}

		final View[] holder = (View[]) view.getTag();
		final String[] from = mFrom;
		final int[] to = mTo;
		final int count = to.length;

        for (int i = 0; i < count; i++) {
            
            final View v = holder[i];
            
            if (v != null) {
               
                final Object data = dataSet.get(from[i]);
                if (data == null) {
                    // make this view invisible if value is empty
                    v.setVisibility(View.GONE);
                    continue;
                } else {
                    v.setVisibility(View.VISIBLE);
                }
                
                 if (v instanceof Checkable) {
                     
                     // 为装机必备记录列表位置
                     v.setTag(position);
                    if (data instanceof Boolean) {
                        ((Checkable) v).setChecked((Boolean) data);
                    } else {
                        throw new IllegalStateException(v.getClass().getName()
                                + " should be bound to a Boolean, not a " + data.getClass());
                    }
                } else if(v instanceof Button) {
                   
                    v.setTag(data);
                    
                } else if(v instanceof ImageButton) {
                    
                    // Note: keep the instanceof ImageButton be Top of ImageView 
                    // since ImageButton is ImageView
// TODO                   setButtonImage((ImageButton) v, text);
                } else if (v instanceof ImageView) {
                    
                    setViewImage(position, (ImageView) v, data);
                    
                } else if (v instanceof RatingBar) {
                  
                    setViewRating((RatingBar)v, data);
                    
                } else if (v instanceof TextView) {
                    
                    // Note: keep the instanceof TextView check at the bottom of
                    // these if since a lot of views are TextViews (e.g. CheckBoxes).
                    setViewText(position, (TextView) v, data);
                    
                } else {
                    throw new IllegalStateException(v.getClass().getName() + " is not a "
                            + " view that can be bounds by this SimpleAdapter");
                }
            }
        }
    }
	
	protected void setViewResource(View v, int position, int[] bitmaps) {
		if (v instanceof ImageView) {
			ImageView view = (ImageView) v;
			HashMap<String, Object> map = mDataSource.get(position);

			int flag = (Integer) map.get(String.valueOf(position));
			view.setImageResource(bitmaps[flag]);
		}
	}

	/*
	 * Set the value for RatingBar
	 */
	private void setViewRating(RatingBar v, Object rating) {
		if (rating instanceof Integer) {
			float ratingLevel = ((Integer) rating) / (float) 10;
			v.setRating(ratingLevel);
		}
	}

	/*
	 * Set text value for TextView
	 */
	private void setViewText(int position, TextView v, Object text) {

		if (text instanceof byte[]) {

			v.setText(Utils.getUTF8String((byte[]) text));

		} else if (text instanceof CharSequence) {

            v.setText((CharSequence) text);

		} else if(text instanceof Integer) {
		    
		    // 应用状态指示器
		    v.setTag(position);
		    final int level = (Integer) text;
		    Drawable indicatorDrawble = v.getCompoundDrawables()[1];
		    indicatorDrawble.setLevel(level);
		    if(Constants.STATUS_NORMAL == level) {
		        // 未下载
                v.setText((String) mDataSource.get(position).get(Constants.KEY_PRODUCT_PRICE));
                
		    } else if(Constants.STATUS_PENDING == level) {
		        // 准备开始下载
                v.setText(mContext.getString(R.string.download_status_downloading));
		        
		    } else if(Constants.STATUS_DOWNLOADED == level) {
		        // 已经下载，未安装
		        v.setText(mContext.getString(R.string.download_status_downloaded));
		        
		    } else if(Constants.STATUS_INSTALLED == level) {
		        // 已经安装
		        v.setText(mContext.getString(R.string.download_status_installed));
		        
		    } else if(Constants.STATUS_UPDATE == level) {
		        
		        // 有更新
		        v.setText(mContext.getString(R.string.operation_update));
		        
		    } else {
		        // 下载中
		        v.setText((String) mDataSource.get(position).get(Constants.KEY_PRODUCT_INFO));
		        
		    }
	        // 为下载按钮绑定事件
            v.setOnClickListener(mDownloadListener);
		}
	}

	/*
	 * Set drawable value for ImageView
	 */
	private void setViewImage(int position, ImageView v, Object obj) {
        
	    Drawable oldDrawable = v.getDrawable();
        if (oldDrawable != null) {
            // clear the CALLBACK reference to prevent of OOM error
            oldDrawable.setCallback(null);
        }
        
        if(obj instanceof Drawable) {
            // here is one drawable object
            v.setImageDrawable((Drawable) obj);
            
        } else if(obj instanceof String) {
            // here is one remote object (URL)
            ImageUtils.download(mContext, (String) obj, v);
        } else if(obj instanceof Boolean) {
            
            if ((Boolean) obj) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.INVISIBLE);
            }
        }
	}

    private OnClickListener mDownloadListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            int position = (Integer) v.getTag();
            HashMap<String, Object> item = mDataSource.get(position);
            int status = (Integer) item.get(Constants.KEY_PRODUCT_DOWNLOAD);
            int payType = (Integer) item.get(Constants.KEY_PRODUCT_PAY_TYPE);
            if (Constants.STATUS_NORMAL == status
                    || Constants.STATUS_UPDATE == status) {
                
                if (Constants.PAY_TYPE_PAID == payType) {
                    if (Session.get(mContext).isLogin()) {
//                        Intent intent = new Intent(mContext, PreloadActivity.class);
//                        intent.putExtra(Constants.EXTRA_PRODUCT_ID,
//                                (String) item.get(Constants.KEY_PRODUCT_ID));
//                        intent.putExtra(Constants.IS_BUY, true);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(intent);
                    } else {
                        Intent loginIntent = new Intent(mContext, RegisterActivity.class);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(loginIntent);
                    }
                } else {
                    
                    Utils.trackEvent(mContext, mPageType, Constants.DIRECT_DOWNLOAD);
                    
                    String pid = (String) item.get(Constants.KEY_PRODUCT_ID);
                    String iconUrl = (String) item.get(Constants.KEY_PRODUCT_ICON_URL_LDPI);
                    String pkgName = (String) item.get(Constants.KEY_PRODUCT_PACKAGE_NAME);
                    String apkUrl  = (String) item.get(Constants.KEY_PRODUCT_APK_URL);
                    // 开始下载，避免用户多次点击
                    item.put(Constants.KEY_PRODUCT_DOWNLOAD, Constants.STATUS_PENDING);
                    mIconCache.put(pkgName, iconUrl);
                    DownloadItem info = new DownloadItem();
                    info.packageName = pkgName;
                    info.pId = pid;
                    info.url = apkUrl;
                    mDownloadExtraInfo.put(pid, item);
                    download(info);
                    notifyDataSetChanged();
                }
                
            } else if (Constants.STATUS_DOWNLOADED == status) {

                // 安装应用
                String packageName = (String) item.get(Constants.KEY_PRODUCT_PACKAGE_NAME);
                String filePath = (String) item.get(Constants.KEY_PRODUCT_INFO);
                DownloadInfo info = mDownloadingTask.get(packageName);
                if (info != null) {
                    Utils.installApk(mContext, new File(info.mFilePath));
                } else if (!TextUtils.isEmpty(filePath)) {
                    Utils.installApk(mContext, new File(filePath));
                }
            } else if(Constants.STATUS_INSTALLED == status) {
                
                // 已经安装，去产品详细页
//                String packageName = (String) item.get(Constants.KEY_PRODUCT_PACKAGE_NAME);
//                Intent detailIntent = new Intent(mContext, PreloadActivity.class);
//                detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                detailIntent.putExtra(Constants.EXTRA_PACKAGE_NAME, packageName);
//                mContext.startActivity(detailIntent);
            }
        }
    };

    private void download(DownloadItem info) {
        HashMap<String, Object> downloadItem = mDownloadExtraInfo.get(info.pId);
        Request request = new Request(Uri.parse(info.url));
        request.setTitle((String)downloadItem.get(Constants.KEY_PRODUCT_NAME));
        request.setPackageName(info.packageName);
        request.setIconUrl(mIconCache.get(info.packageName));
        request.setSourceType(com.xiaohong.kulian.common.download.Constants.DOWNLOAD_FROM_MARKET);
        request.setMD5(info.fileMD5);
        mDownloadManager.enqueue(request);
        Utils.makeEventToast(mContext, mContext.getString(R.string.download_start), false);
    }
    
    @Override
    public void onSuccess(int method, Object obj) {
        switch (method) {

        default:
            break;
        }
    }

    @Override
    public void onError(int method, int statusCode) {
        Utils.makeEventToast(mContext, mContext.getString(R.string.alert_dialog_error), false);
    }
	
	/**
	 * 下载状态更新，刷新列表状态
	 */
    @SuppressWarnings("unchecked")
    @Override
    public void update(Observable arg0, Object arg1) {
        
        if (arg1 instanceof HashMap) {
            mDownloadingTask = (HashMap<String, DownloadInfo>) arg1;
            notifyDataSetChanged();
        } else if(arg1 instanceof Integer) {
            notifyDataSetChanged();
        }
    }
	
	/*
	 * 是否分隔符
	 */
	private boolean isPlaceHolder(int position) {
	    HashMap<String, Object> item = mDataSource.get(position);
        return (Boolean) item.get(Constants.KEY_PLACEHOLDER);
	}

	/**
	 * Lazyload linstener If you want use the lazyload function, must implements
	 * this interface
	 */
	public interface LazyloadListener {

		/**
		 * You should implements this method to justify whether should do
		 * lazyload
		 * 
		 * @return
		 */
		boolean isEnd();

		/**
		 * Do something that process lazyload
		 */
		void lazyload();

		/**
		 * Indicate whether the loading process is over
		 * 
		 * @return
		 */
		boolean isLoadOver();
	}

}