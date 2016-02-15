package com.mappn.gfan.ui;

import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mappn.gfan.R;
import com.mappn.gfan.common.util.TopBar;

public class ChargeTypeListActivity extends ListActivity {

	// 支付宝
	public static final String TYPE_ALIPAY = "alipay";
	// 神州付电话卡
	public static final String TYPE_PHONECARD = "phonecard";
	// public static final String TYPE_G = "g";

	// 支持的支付类型<标示，名称>
	private HashMap<String, String> mChargeTypes;

	private TextView mTvInfoTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.market_activity_charge_type_list);

		// top bar
        TopBar.createTopBar(this, new View[] { 
                findViewById(R.id.top_bar_title) },
                new int[] { View.VISIBLE }, 
                getString(R.string.choose_charge_type));

		mTvInfoTitle = (TextView) findViewById(R.id.tv_infoTitle);

		showError(getIntent());

		// 占位,为了显示列表下方的横线
		TextView holder = new TextView(this);
		holder.setHeight(1);
		getListView().addFooterView(holder, null, true);

		setListAdapter(new ArrayAdapter<String>(this, 
		        R.layout.market_list_item_category,
				R.id.tv_name, 
				getChargeTypeStrings()));
	}

	/**
	 * 如果充值失败，提示错误
	 */
	private void showError(Intent intent) {
		if (intent.hasExtra("error")) {
			String type = getIntent().getStringExtra("error");
			String name = getChargeType(type);
			mTvInfoTitle.setText(getString(R.string.charge_error, name));
			mTvInfoTitle.setVisibility(View.VISIBLE);
		} else {
			mTvInfoTitle.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, PayMainActivity.class);
		intent.putExtra("type", getType(position));
		if (getIntent().hasExtra("balance")) {
			intent.putExtra("balance", getIntent().getIntExtra("balance", 0));
		}
		startActivityForResult(intent, 0);

		mTvInfoTitle.setVisibility(View.GONE);
	}

	protected String getType(int position) {
		switch (position) {
		case 0:
			return TYPE_ALIPAY;
		case 1:
			return TYPE_PHONECARD;
			/*
			 * case 3: return PrefUtil.TYPE_G;
			 */
		default:
			return null;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.hasExtra("balance")) {
			getIntent().putExtra("balance", intent.getExtras().getInt("balance"));
		}
		showError(intent);
	}

	/**
	 * 获取支付类型的名称
	 * 
	 * @param key
	 *            支付类型标示
	 * */
	public String getChargeType(String key) {
		return mChargeTypes.get(key);
	}

	/**
	 * 获取所有支付类型的名称
	 * */
	private String[] getChargeTypeStrings() {
		mChargeTypes = new HashMap<String, String>(2);

		String str1 = getString(R.string.charge_alipay);
		String str2 = getString(R.string.charge);

		mChargeTypes.put(TYPE_ALIPAY, str1);
		mChargeTypes.put(TYPE_PHONECARD, str2);

		return new String[] { str1, str2, /* "G币兑换机锋券充值" */};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Activity.RESULT_OK == resultCode && 0 == requestCode) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}
}