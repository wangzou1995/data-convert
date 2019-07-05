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
		JSONObject sourceObject = source.getJSONObject("tb_window");
		Set<String> keys = sourceObject.keySet();
		keys.forEach(key -> {
			if (!key.equals("tb_window_layout")) {
				window.put(key, sourceObject.get(key));
			} else {
				window.put(key, initLayout(sourceObject.getJSONArray(key)));
			}
		});
		// 添加tb_window节点
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(window);
		JSONObject resultObj = new JSONObject();
		resultObj.put("tb_window", jsonArray);
		return resultObj;
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
				// 设置是否末级
				object.put("leaf", true);
				break;
			case "grid":
				// 取出工具容器
				JSONArray tools = object.getJSONArray("tb_tool_element");
				if (tools.size() > 0) {
					resultArray.add(tools.get(0));
					object.put("leaf", false);
				} else {
					object.put("leaf", true);
				}
				// 移除
				object.remove("tb_tool_element");
				object.remove("toolPosition");
				object.remove("isShowTool");
				break;
			case "tabpanel":
			case "panel":
			case "container":
				JSONArray lays = object.getJSONArray("tb_window_element");

				if (lays.size() > 0) {
					resultArray.addAll(initLayout(object.getJSONArray("tb_window_element")));
					object.put("leaf", false);
					object.put("tb_window_element", new JSONArray());
				} else {
					object.put("leaf", true);
				}
				break;
			default:
				break;
			}
			object.remove("name");
			object.remove("type");
			object.remove("icon");
			JSONArray array = object.getJSONArray("tb_window_element");
			if (array.size() > 0) {
				elementRemoveNode(array);
			}
			resultArray.add(object);
		});
		return resultArray;
	}

	/**
	 * 删除元素节点
	 * 
	 * @param array
	 */
	private static void elementRemoveNode(JSONArray array) {
		array.forEach(obj -> {
			JSONObject object = (JSONObject) obj;
			object.remove("icon");
		});
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
