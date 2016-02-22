package com.xiaohong.kulian.common.vo;

import java.util.ArrayList;

public class PayAndChargeLogs {
	public int endPosition;
	public int totalSize;
	public ArrayList<PayAndChargeLog> payAndChargeLogList;

	public PayAndChargeLogs() {
		payAndChargeLogList = new ArrayList<PayAndChargeLog>();
	}

}
