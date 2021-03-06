package com.teamkn.base.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.teamkn.R;

public class ArrayListMenu {
	public static ArrayList<Map<String, Object>> getData() {
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "我的首页");
		map.put("img", R.drawable.mi_menu_home);
		
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "我的列表");
		map.put("img", R.drawable.mi_menu_data_list);

		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "公开的列表");
		map.put("img", R.drawable.mi_menu_friendly);
		
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", "社交中心");
		map.put("img", R.drawable.mi_menu_exchange_manager);
		
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "设置选项");
		map.put("img", R.drawable.mi_menu_setting);

		list.add(map);

		
		
//		-----------------------------------------------------
//		map = new HashMap<String, Object>();
//		map.put("title", "用户信息");
//		map.put("img", R.drawable.mi_menu_usermsg);
//
//		list.add(map);
//		map = new HashMap<String, Object>();
//		map.put("title", "注销登录");
//		map.put("img", R.drawable.mi_menu_account_re);
//
//		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "退出应用");
		map.put("img", R.drawable.mi_menu_exit);

		list.add(map);

		return list;
	}
}
