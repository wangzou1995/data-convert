package cn.com.yw56.dataconvert.until;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Vue2ExtUntil {
	public static JSONArray vue2ExtData(JSONObject source) {
		JSONArray resultArray = new JSONArray();
		// 传入tb_window_layout节点数据
		JSONArray layoutArray = source.getJSONArray("tb_window_layout");
		if (layoutArray.size() > 0) {
			resultArray = initLayout(layoutArray);
		}
		return resultArray;
	}

	private static JSONArray initLayout(JSONArray layoutArray) {
		JSONArray resultArray = new JSONArray();
		layoutArray.forEach(layout -> {
			JSONObject object = (JSONObject) layout;
			String containerType = object.getString("type");
			if ("grid".equals(containerType)) {
				
			}
		});
		return resultArray;
	}
}
