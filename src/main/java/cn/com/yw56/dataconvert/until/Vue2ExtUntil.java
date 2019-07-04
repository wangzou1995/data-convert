package cn.com.yw56.dataconvert.until;

import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Vue2ExtUntil {
	/**
	 * vue数据转换ext数据
	 * 
	 * @param source
	 * @return
	 */
	public static JSONObject vue2ExtData(JSONObject source) {
		JSONObject window = new JSONObject();
		Set<String> keys = source.keySet();
		keys.forEach(key -> {
			if (!key.equals("tb_window_layout")) {
				window.put(key, source.get(key));
			} else {
				window.put(key, initLayout(source.getJSONArray(key)));
			}
		});
		return window;
	}

	/**
	 * 初始化布局容器
	 * 
	 * @param layoutArray
	 * @return
	 */
	private static JSONArray initLayout(JSONArray layoutArray) {
		JSONArray resultArray = new JSONArray();
		layoutArray.forEach(layout -> {
			JSONObject object = (JSONObject) layout;
			String containerType = object.getString("type");
			switch (containerType) {
			case "form":
			case "searchfrom":
				object.put("tb_window_element", deleteRowContainer(object.getJSONArray("tb_window_element")));
				break;
			case "grid":
				// 取出工具容器
				resultArray.add(object.getJSONArray("tb_tool_element").get(0));
				// 移除
				System.out.println(1);
				object.remove("tb_tool_element");
				object.remove("toolPosition");
				object.remove("isShowTool");
				break;
			case "tabpanel":
			case "panel":
				resultArray.addAll(initLayout(object.getJSONArray("tb_window_element")));
				object.put("tb_window_element", new JSONArray());
				break;
			default:
				break;
			}
			resultArray.add(object);
		});
		return resultArray;
	}

	/**
	 * 删除容器
	 * 
	 * @param jsonArray
	 * @return
	 */
	private static JSONArray deleteRowContainer(JSONArray jsonArray) {
		JSONArray resultArray = new JSONArray();
		if (jsonArray.size() > 0) {
			jsonArray.forEach(row -> {
				JSONArray colArray = ((JSONObject) row).getJSONArray("row");
				if (colArray.size() > 0) {
					colArray.forEach(col -> {
						resultArray.add(((JSONObject) col).getJSONArray("col").getJSONObject(0));
					});
				}
			});
		}
		return resultArray;
	}
}
