package com.xiaohong.kulian.common.vo;

import android.content.ContentValues;

import com.xiaohong.kulian.common.util.MarketProvider;

public class CardsVerification {
	public String name;
	public String pay_type;
	public int accountNum; // 卡号位数
	public int passwordNum; // 密码位数
	public String credit;// 面额

	public void onAddToDatabase(ContentValues values) {
		values.put(MarketProvider.COLUMN_CARD_NAME, name);
		values.put(MarketProvider.COLUMN_CARD_PAY_TYPE, pay_type);
		values.put(MarketProvider.COLUMN_CARD_ACCOUNTNUM, accountNum);
		values.put(MarketProvider.COLUMN_CARD_PASSWORDNUM, passwordNum);
		values.put(MarketProvider.COLUMN_CARD_CREDIT, credit);
	}
}
