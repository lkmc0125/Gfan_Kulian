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
package com.xiaohong.kulian.common.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.xiaohong.kulian.R;

/**
 * 带title的Spinner
 * 
 * @author libin
 * @date 2010-12-22
 * @since Version 0.5.0
 */
public class TitleSpinner extends Button implements OnClickListener {

	private CharSequence mPrompt;
	private SpinnerAdapter adapter;
	private int mNextSelectedPosition;
	private android.content.DialogInterface.OnClickListener mOnClickListener;

	public TitleSpinner(Context context) {
		super(context);
		mNextSelectedPosition = -1;
		setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		setBackgroundResource(android.R.drawable.btn_dropdown);
	}

	public TitleSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);

		mNextSelectedPosition = -1;
		setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		setBackgroundResource(android.R.drawable.btn_dropdown);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleSpinner);

		if (a.hasValue(R.styleable.TitleSpinner_prompt)) {
			mPrompt = a.getString(R.styleable.TitleSpinner_prompt);
		}
		a.recycle();
	}

	public TitleSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mNextSelectedPosition = -1;
		setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		setBackgroundResource(android.R.drawable.btn_dropdown);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleSpinner);

		if (a.hasValue(R.styleable.TitleSpinner_prompt)) {
			mPrompt = a.getString(R.styleable.TitleSpinner_prompt);
		}

		a.recycle();
	}

	public int getSelectedItemPosition() {
		return mNextSelectedPosition;
	}

	public void setSelection(int position) {
		mNextSelectedPosition = position;
		setText(adapter.getItem(position).toString());
		// Theme theme = this.getContext().getTheme();
		// TypedValue tv = new TypedValue();
		// if (theme.resolveAttribute(android.R.attr.textColorSecondary, tv,
		// true))
		// setTextColorthis.getContext().getResources().getColor(tv.resourceId)t);

		//setTextColor(android.R.color.primary_text_light);
	}

	public void setAdapter(SpinnerAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		setSelection(which);
		dialog.dismiss();
		if (mOnClickListener != null)
			mOnClickListener.onClick(dialog, which);
	}

	@Override
	public boolean performClick() {
		boolean handled = super.performClick();
		if (!handled) {
			handled = true;

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			if (mPrompt != null) {
				builder.setTitle(mPrompt);
			}

			builder.setSingleChoiceItems(new DropDownAdapter(adapter), getSelectedItemPosition(), this).show();
		}

		return handled;
	}

	public void setOnClickListener(android.content.DialogInterface.OnClickListener listener) {
		mOnClickListener = listener;
	}

	/**
	 * <p>
	 * Wrapper class for an Adapter. Transforms the embedded Adapter instance
	 * into a ListAdapter.
	 * </p>
	 */
	private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
		private SpinnerAdapter mAdapter;
		private ListAdapter mListAdapter;

		/**
		 * <p>
		 * Creates a new ListAdapter wrapper for the specified adapter.
		 * </p>
		 * 
		 * @param adapter
		 *            the Adapter to transform into a ListAdapter
		 */
		public DropDownAdapter(SpinnerAdapter adapter) {
			this.mAdapter = adapter;
			if (adapter instanceof ListAdapter) {
				this.mListAdapter = (ListAdapter) adapter;
			}
		}

		public int getCount() {
			return mAdapter == null ? 0 : mAdapter.getCount();
		}

		public Object getItem(int position) {
			return mAdapter == null ? null : mAdapter.getItem(position);
		}

		public long getItemId(int position) {
			return mAdapter == null ? -1 : mAdapter.getItemId(position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return getDropDownView(position, convertView, parent);
		}

		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return mAdapter == null ? null : mAdapter.getDropDownView(position, convertView, parent);
		}

		public boolean hasStableIds() {
			return mAdapter != null && mAdapter.hasStableIds();
		}

		public void registerDataSetObserver(DataSetObserver observer) {
			if (mAdapter != null) {
				mAdapter.registerDataSetObserver(observer);
			}
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {
			if (mAdapter != null) {
				mAdapter.unregisterDataSetObserver(observer);
			}
		}

		/**
		 * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this
		 * call. Otherwise, return true.
		 */
		public boolean areAllItemsEnabled() {
			final ListAdapter adapter = mListAdapter;
			if (adapter != null) {
				return adapter.areAllItemsEnabled();
			} else {
				return true;
			}
		}

		/**
		 * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this
		 * call. Otherwise, return true.
		 */
		public boolean isEnabled(int position) {
			final ListAdapter adapter = mListAdapter;
			if (adapter != null) {
				return adapter.isEnabled(position);
			} else {
				return true;
			}
		}

		public int getItemViewType(int position) {
			return 0;
		}

		public int getViewTypeCount() {
			return 1;
		}

		public boolean isEmpty() {
			return getCount() == 0;
		}
	}

	public void setPrompt(CharSequence prompt) {
		mPrompt = prompt;
	}
}