package com.xiaohong.kulian.common.util;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;

public class MySharedpreference {

	private Context context;

	public MySharedpreference(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	public boolean saveRankTabActivity_FLAG(String Flag) {
            boolean flag = false;
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                            "RankTabActivity_FLAG", Context.MODE_PRIVATE);
            // 对数据进行编辑
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("type", Flag);
            flag = editor.commit();// 将数据持久化到存储介质中
            return flag;
    }   
	public Map<String, Object> getMessage() {
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"RankTabActivity_FLAG", Context.MODE_PRIVATE);
		String type = sharedPreferences.getString("type", "");
		map.put("type", type);
		return map;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> get_base_data_Message() {
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"base_data", Context.MODE_PRIVATE);
		map=(Map<String, Object>) sharedPreferences.getAll();
		String type = sharedPreferences.getString("type", "");
		map.put("type", type);
		return map;
	}
	
	
}
