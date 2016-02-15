package com.mappn.gfan.common.vo;

public class PayAndChargeLog {
	public static final int TYPE_CONSUME = 1;
	public static final int TYPE_MARKET = 2;
	public static final int TYPE_CHARGE = 3;

	public String name;
	public String desc;
	public String time;
	public int payment;
	public int type;
	public int id;
	public String iconUrl;
	public int sourceType;

	public PayAndChargeLog() {
		type = TYPE_MARKET;
	}
}