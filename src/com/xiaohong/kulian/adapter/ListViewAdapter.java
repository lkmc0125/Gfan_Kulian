package com.xiaohong.kulian.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import aga.fdf.grd.os.df.DiyAppNotify;
import aga.fdf.grd.os.df.AdExtraTaskStatus;
import aga.fdf.grd.os.df.AdTaskStatus;
import aga.fdf.grd.os.df.AppExtraTaskObject;
import aga.fdf.grd.os.df.AppExtraTaskObjectList;
import aga.fdf.grd.os.df.AppSummaryObject;
import aga.fdf.grd.os.df.DiyOfferWallManager;

import java.util.ArrayList;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.vo.CustomObject;

/**
 * 广告列表页Adapter
 *
 * @author youmi
 * @date 2015-4-24 上午9:12:48
 */
public class ListViewAdapter extends BaseAdapter implements DiyAppNotify {

	private Context mContext;

	private ArrayList<CustomObject> mCustomObjectArrayList;

	private SparseArray<ViewHolder> mViewHolderList = new SparseArray<ListViewAdapter.ViewHolder>();

	public ListViewAdapter(Context context, ArrayList<CustomObject> mCustomObjectArrayList) {
		this.mContext = context;
		this.mCustomObjectArrayList = mCustomObjectArrayList;
	}

	public void addData(ArrayList<CustomObject> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		if (mCustomObjectArrayList == null) {
			mCustomObjectArrayList = new ArrayList<CustomObject>();
		}
		mCustomObjectArrayList.addAll(list);
	}

	public ArrayList<CustomObject> getData() {
		return mCustomObjectArrayList;
	}

	public void reset() {
		mCustomObjectArrayList = null;
	}

	@Override
	public int getCount() {
		return mCustomObjectArrayList == null ? 0 : mCustomObjectArrayList.size();
	}

	@Override
	public CustomObject getItem(int position) {
		if (mCustomObjectArrayList == null || mCustomObjectArrayList.isEmpty()) {
			return null;
		}
		return mCustomObjectArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.lv_item_normal_task, null);
			holder = new ViewHolder();
			holder.app_icon = (ImageView) convertView.findViewById(R.id.lvitem_iv_appicon);
			holder.app_name = (TextView) convertView.findViewById(R.id.lvitem_tv_appname);
			holder.app_adSlogan = (TextView) convertView.findViewById(R.id.lvitem_tv_appslogan);
			holder.app_adpoints = (TextView) convertView.findViewById(R.id.lvitem_tv_adpoints);
			holder.app_status = (TextView) convertView.findViewById(R.id.lvitem_tv_appstatus);
			holder.app_download_progress = (ProgressBar) convertView.findViewById(R.id.lvitem_pb_download);
			holder.app_download_btn = (Button) convertView.findViewById(R.id.lvitem_btn_download);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CustomObject item = getItem(position);

		final AppSummaryObject appSummaryObject = item.getAppSummaryObject();
		holder.id = appSummaryObject.getAdId();
		holder.app_download_progress.setVisibility(View.GONE);
		mViewHolderList.put(appSummaryObject.getAdId(), holder);

		// 设置广告图标
		if (item.getAppicon() == null) {
			holder.app_icon.setVisibility(View.INVISIBLE);
		} else {
			holder.app_icon.setImageBitmap(item.getAppicon());
			holder.app_icon.setVisibility(View.VISIBLE);
		}

		// 设置广告名字
		holder.app_name.setText(appSummaryObject.getAppName());

		// 设置广告语
		holder.app_adSlogan.setText(appSummaryObject.getAdSlogan());

		String action_type = "";

		// 设置按钮是"打开"还是"下载安装"
		final boolean isPackageExist = Utils.isApkInstalled(mContext, appSummaryObject.getPackageName());
		holder.app_download_btn.setText(isPackageExist ? "打开" : "下载安装");

		// 设置广告的状态、广告语
		switch (appSummaryObject.getAdTaskStatus()) {

		// 未完成
		case AdTaskStatus.NOT_COMPLETE:
			holder.app_status.setText("未完成");
			holder.app_status.setTextColor(mContext.getResources().getColor(R.color.black));

			// 这里将演示将正常任务的积分和追加任务的积分加起来，然后展示给用户，开发者可以参考这里使用
			String textformat = "<html><body>" + action_type + "+<b><font color=\"#FF9F05\">" +
			                    getTotalPoints(getItem(position).getAppSummaryObject()) + "</b>积分</body></html>";
			holder.app_adpoints.setText(Html.fromHtml(textformat));
			holder.app_adpoints.setVisibility(View.VISIBLE);

			break;

		// 已完成
		case AdTaskStatus.ALREADY_COMPLETE:
			holder.app_status.setText("已完成");
			holder.app_status.setTextColor(mContext.getResources().getColor(R.color.green_color));

			holder.app_adpoints.setVisibility(View.GONE);

			break;

		// 有追加任务
		case AdTaskStatus.HAS_EXTRA_TASK:
			// 如果是同一个广告需要展示多次的话，就标识这个item是专门请求追加任务的item，需要特殊处理
			if (getItem(position).isShowMultSameAd()) {
				AppExtraTaskObjectList extraTaskList =
						getItem(position).getAppSummaryObject().getExtraTaskList();
				if (extraTaskList != null && extraTaskList.size() > 0) {
					AppExtraTaskObject extraTaskObject = extraTaskList.get(getItem(position).getShowExtraTaskIndex());
					if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START) {
						holder.app_status.setText("任务等待中");
						holder.app_status.setTextColor(Color.parseColor("#BFBFBF"));

						holder.app_adpoints.setText("完成+" + extraTaskObject.getPoints() + "积分");
						holder.app_adpoints.setTextColor(Color.parseColor("#C0C0C0"));

					} else if (extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {
						holder.app_status.setText("任务进行中");
						holder.app_status.setTextColor(Color.parseColor("#8256D9"));

						holder.app_adpoints.setTextColor(Color.parseColor("#399A00"));
						String textformat1 = "<html><body>+<b><font color=\"#BE0028\">" + extraTaskObject.getPoints() +
						                     "</b>积分</body></html>";
						holder.app_adpoints.setText(Html.fromHtml(textformat1));
						holder.app_adpoints.setVisibility(View.VISIBLE);

					}
					holder.app_adSlogan.setText(extraTaskObject.getAdText());
				}
			}

			// 到这里就标识是其他广告列表请求
			else {

				AppExtraTaskObjectList extraTaskList =
						getItem(position).getAppSummaryObject().getExtraTaskList();

				if (extraTaskList != null && extraTaskList.size() > 0) {

					for (int i = 0; i < extraTaskList.size(); ++i) {
						AppExtraTaskObject extraTaskObject = extraTaskList.get(i);
						if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START ||
						    extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {

							holder.app_status.setText("追加奖励");
							holder.app_status.setTextColor(mContext.getResources().getColor(R.color.black));

							String textformat1 =
									"<html><body>+<b><font color=\"#BE0028\">" + extraTaskObject.getPoints() +
									"</b>积分</body></html>";
							holder.app_adpoints.setText(Html.fromHtml(textformat1));
							holder.app_adpoints.setVisibility(View.VISIBLE);

							holder.app_adSlogan.setText(extraTaskObject.getAdText());
							break;
						}
					}
				}
			}

			break;
		default:
			break;
		}

		holder.app_download_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				DiyOfferWallManager.getInstance(mContext)
						.openOrDownloadApp((Activity) mContext, appSummaryObject);
			}
		});

		return convertView;

	}

	private static class ViewHolder {

		int id;

		ImageView app_icon;

		TextView app_name;

		TextView app_adSlogan;

		TextView app_adpoints;

		TextView app_status;

		ProgressBar app_download_progress;

		Button app_download_btn;
	}

	/**
	 * 如果任务未完成就获取指定广告的所有积分（正常完成的积分+可完成的追加任务积分）
	 */
	private int getTotalPoints(AppSummaryObject appSummaryObject) {
		int totalpoints = appSummaryObject.getPoints();
		AppExtraTaskObjectList tempList = appSummaryObject.getExtraTaskList();
		if (tempList != null && tempList.size() > 0) {
			for (int i = 0; i < tempList.size(); ++i) {
				AppExtraTaskObject extraTaskObject = tempList.get(i);
				if (extraTaskObject.getStatus() == AdExtraTaskStatus.NOT_START ||
				    extraTaskObject.getStatus() == AdExtraTaskStatus.IN_PROGRESS) {
					totalpoints += extraTaskObject.getPoints();
				}
			}
		}
		return totalpoints;
	}

	@Override
	public void onDownloadStart(int id) {

	}

	@Override
	public void onDownloadProgressUpdate(int id, long contentLength, long completeLength, int percent, long speedBytesPerS) {
		try {
			ViewHolder viewHolder = mViewHolderList.get(id);
			if (viewHolder == null || viewHolder.id != id) {
				return;
			}
			viewHolder.app_download_progress.setProgress(percent);
			viewHolder.app_download_progress.setVisibility(View.VISIBLE);

			viewHolder.app_download_btn.setEnabled(false);
			viewHolder.app_download_btn.setText("正在下载");

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDownloadSuccess(int id) {
		try {
			ViewHolder viewHolder = mViewHolderList.get(id);
			if (viewHolder == null || viewHolder.id != id) {
				return;
			}
			viewHolder.app_download_progress.setProgress(0);
			viewHolder.app_download_progress.setVisibility(View.GONE);
			viewHolder.app_status.setText("下载成功,请安装!");

			viewHolder.app_download_btn.setEnabled(true);
			viewHolder.app_download_btn.setText("安装");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDownloadFailed(int id) {
		try {
			ViewHolder viewHolder = mViewHolderList.get(id);
			if (viewHolder == null || viewHolder.id != id) {
				return;
			}
			viewHolder.app_download_progress.setProgress(0);
			viewHolder.app_download_progress.setVisibility(View.GONE);
			viewHolder.app_status.setText("下载失败,请重试!");

			viewHolder.app_download_btn.setEnabled(true);
			viewHolder.app_download_btn.setText("下载安装");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInstallSuccess(int id) {
		try {
			ViewHolder viewHolder = mViewHolderList.get(id);
			if (viewHolder == null || viewHolder.id != id) {
				return;
			}
			viewHolder.app_download_progress.setProgress(0);
			viewHolder.app_download_progress.setVisibility(View.GONE);
			viewHolder.app_status.setText("安装成功!");

			viewHolder.app_download_btn.setEnabled(true);
			viewHolder.app_download_btn.setText("打开");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
