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
package com.mappn.gfan.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mappn.gfan.Constants;
import com.mappn.gfan.R;
import com.mappn.gfan.Session;
import com.mappn.gfan.common.util.ImageUtils;
import com.mappn.gfan.common.vo.PayAndChargeLog;

/**
 * this adapter for personal center
 * 
 * @author cong.li
 * @date 2011-5-17
 * 
 */
public class PersonalAccountAdapter extends BaseAdapter {

	private ArrayList<HashMap<String, Object>> mDataSource;
	private int mResource;
	private String[] mFrom;
	private int[] mTo;
	private LayoutInflater mInflater;
	private Context mContext;
	private Session mSession;
	private Handler mHandler;

	private final WeakHashMap<View, View[]> mHolders = new WeakHashMap<View, View[]>();

	PersonalAccountAdapter(Context context,
			ArrayList<HashMap<String, Object>> data, int resource,
			String[] from, int[] to, Handler handler) {

		if (data == null) {
			mDataSource = new ArrayList<HashMap<String, Object>>();
		} else {
			mDataSource = data;
		}

		mContext = context;
		mResource = resource;
		mFrom = from;
		mTo = to;
		mHandler = handler;
		mSession = Session.get(context);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

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
	public int getItemViewType(int position) {
		if (!isGroupOrHeader(position, Constants.KEY_PLACEHOLDER)) {
			// normal
			return 0;
		}
		if (isGroupOrHeader(position, Constants.HEADER_ITEM)) {
			// header item
			return 2;
		} else {
			// place holder
			return 1;
		}
	}

	private boolean isGroupOrHeader(int position, String itemFlag) {
		if (mDataSource == null) {
			return false;
		}
		HashMap<String, Object> forumEntry = mDataSource.get(position);
		if (forumEntry == null) {
			return false;
		}

		Object subOnly = forumEntry.get(itemFlag);
		if (subOnly == null) {
			// you maybe forgot to put the flag
			return false;
		}

		if (subOnly instanceof String) {
			return Boolean.valueOf((String) subOnly);
		} else if (subOnly instanceof Boolean) {
			return (Boolean) subOnly;
		}
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		assert (position < getCount());
		View v = null;
		int viewType = (Integer) mDataSource.get(position).get(
				Constants.ACCOUNT_TYPE);
		if (convertView == null) {
			v = newView(viewType, parent, position);
		} else {
			int viewTag = (Integer) convertView.getTag();
			if (Constants.FLAG_HEADER_ITEM == viewType) {
				v = newView(viewType, parent, position);
			}
			if (viewTag != viewType) {
				v = newView(viewType, parent, position);
			} else {
				v = convertView;
			}
		}
		
		bindView(position, v);
		return v;
	}

	private void bindView(int position, View v) {
		final HashMap<String, Object> dataSet = mDataSource.get(position);
		if (dataSet == null) {
			return;
		}

		final View[] holder = mHolders.get(v);
		final String[] from = mFrom;
		final int[] to = mTo;
		final int count = to.length;

		for (int i = 0; i < count; i++) {

			final View view = holder[i];
			if (view != null) {

				final Object data = dataSet.get(from[i]);
				int res = 0;
				Drawable drawable = null;

				if (view instanceof TextView) {
					TextView txt = (TextView) view;
					setTextViewValue(txt, data, position);

				} else if (view instanceof ImageView) {
					ImageView iv = (ImageView) view;
					if (data instanceof Integer) {
						res = (data == null ? R.drawable.down_btn_1
								: (Integer) data);
						iv.setImageResource(res);
					} else if (data instanceof Drawable) {
						drawable = data == null ? mContext.getResources()
								.getDrawable(R.drawable.down_btn_2)
								: (Drawable) data;
						iv.setImageDrawable(drawable);
					} else if (data instanceof String) {
                        ImageUtils.download(mContext, (String) data, iv);
					} else if (data instanceof Boolean) {
						if ((Boolean) data && position == 0) {
							iv.setVisibility(View.VISIBLE);
						} else {
							iv.setVisibility(View.GONE);
						}
					}
				}
			}
		}
	}

	private void setTextViewValue(TextView txt, Object data, int position) {
		if (mSession.isLogin() && position == 0) {
			if (txt.getId() == R.id.tv_name)
				txt.setText(mSession.getUserName());
			if (txt.getId() == R.id.tv_description)
				txt.setText(mContext.getString(R.string.account_logined));
		} else {
			txt.setText((CharSequence) data);
		}
	}

	/**
	 * 判断是否是group，将不同的view与当前的position绑定
	 */
	private View newView(int viewType, ViewGroup parent, int position) {
		View v = null;
		switch (viewType) {
		
		// ListView holder
		case Constants.FLAG_GROUP_ITEM:
			v = mInflater.inflate(
					R.layout.activity_install_nessary_list_separator, parent,
					false);
			break;
			
		// ListView header item
		case Constants.FLAG_HEADER_ITEM:
			v = mInflater.inflate(mResource, parent, false);
			break;

		case Constants.FLAG_NO_PAY_LOG_ITEM:
			v = mInflater.inflate(
					R.layout.activity_personal_account_no_pay_item, parent,
					false);
			break;
		// ListView pay or charge
		case PayAndChargeLog.TYPE_CONSUME:
        case PayAndChargeLog.TYPE_CHARGE:
            v = mInflater.inflate(R.layout.activity_personal_account_login_paycharge_item, parent,
                    false);
            break;
			
		// ListView pay normal
		case PayAndChargeLog.TYPE_MARKET:
			v = mInflater.inflate(
					R.layout.activity_personal_account_login_item, parent,
					false);
			break;

		default:
			v = mInflater.inflate(mResource, parent, false);
			break;
		}

		final int[] to = mTo;
		final int count = to.length;
		final View[] holder = new View[count];

		for (int i = 0; i < count; i++)
			holder[i] = v.findViewById(to[i]);

		if (mSession.isLogin()) {
			v.findViewById(R.id.iv_arrow).setVisibility(View.VISIBLE);
			v.findViewById(R.id.cb_operation).setVisibility(View.GONE);
		} else {
			if (position == 0)
				v.findViewById(R.id.iv_arrow).setVisibility(View.VISIBLE);
			if (position == 1) {
				v.findViewById(R.id.iv_arrow).setVisibility(View.VISIBLE);
				v.findViewById(R.id.cb_operation).setVisibility(View.GONE);
			}
		}
		
		mHolders.put(v, holder);
		v.setTag(viewType);
		return v;
	}
	
	@Override
	public boolean isEnabled(int position) {
		int viewType = (Integer) mDataSource.get(position).get(
				Constants.ACCOUNT_TYPE);
		switch (viewType) {
		case Constants.FLAG_GROUP_ITEM:
			return false;
		case Constants.FLAG_HEADER_ITEM:
			if (mSession.isLogin()) {
				return true;
			}
			if (!(mSession.isLogin()) && position == 0) {
				return true;
			}

			return false;
		default:
			break;
		}
		return true;
	}

	public void changeDataSource(List<HashMap<String, Object>> data) {
		if (data != null && data.size() > 0) {
			mDataSource.clear();
			mDataSource.addAll(data);
			notifyDataSetChanged();
		}
	}

	public void clearData() {
		mDataSource.clear();
		notifyDataSetChanged();
	}
	
	public ArrayList<HashMap<String, Object>> getDataSource() {
		return mDataSource;
	}
	
	public void addData(List<HashMap<String, Object>> newData) {

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
}
