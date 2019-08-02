package cn.com.yw56.dataconvert.until;

import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * Vue数据转换工具类
 * @author wangzou1995
 *
 */
public class Vue2ExtUntil {

	/**
	 * vue数据转换ext数据
	 * 
	 * @param source
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
		// 窗口节点需要JSON转换成字符串
		String[] tempStrings = { "filter" };
		exchangeJSON2String(tempStrings, window);
		// 添加tb_window节点
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(window);
		JSONObject resultObj = new JSONObject();
		resultObj.put("tb_window", jsonArray);
		resultObj.put("removeObjs", source.getJSONArray("removeObjs"));
		return resultObj;
	}

	/**
	 * JSO转换字符串
	 * 
	 * @param string
	 * @param object
	 */
	private static void exchangeJSON2String(String[] strings, JSONObject object) {
		for (int i = 0; i < strings.length; i++) {
			Object o = object.get(strings[i]);
			if (o != null && !"[]".equals(o.toString())) {
				object.put(strings[i], o.toString());
			} else {
				object.put(strings[i], null);
			}
		}
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
			case "searchform":
				object.put("tb_window_element", deleteRowContainer(object.getJSONArray("tb_window_element")));
				// 设置是否末级
				object.put("leaf", true);
				break;
			case "grid":
				// 取出工具容器
				JSONArray tools = object.getJSONArray("tb_tool_element");
				if (tools != null && tools.size() > 0) {
					JSONObject tool = tools.getJSONObject(0);
					tool.remove("name");
					tool.remove("type");
					tool.remove("icon");
					//删除里面的
					JSONArray toolElementsArray = tool.getJSONArray("tb_window_element");
					for(int i = 0 , max = toolElementsArray.size(); i < max; i++) {
						JSONObject toolElement = toolElementsArray.getJSONObject(i);
						toolElement.remove("icon");
						toolElement.remove("name");
					}
					resultArray.add(tool);
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
			case "cardpanel":
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
			// JSON转换 字符串
			String[] temp = { "filter", "filtersql" };
			exchangeJSON2String(temp, object);
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
		String[] temps = { "visible", "elementstatus" };
		array.forEach(obj -> {
			JSONObject object = (JSONObject) obj;
			object.remove("icon");
			object.remove("name");
			// 表格元素删除
			object.remove("parentid");
			object.remove("parenttype");
			
			exchangeJSON2String(temps, object);
			Set<String> keys = object.keySet();
			keys.forEach(key -> {
				switch (key) {
				case "tb_window_formatsearch":
					baseElementScript(object, key, "script");
					break;
				case "tb_window_element_action":
					actionJSON2Str(object.getJSONArray(key));
					break;
				case "tb_window_selectfields":
					baseElementScript(object, key, "datasource");
					break;
				case "tb_window_inputfields":
					baseElementScript(object, key, "switchfilter");
					baseElementScript(object, key, "filter");
					break;
				default:
					break;
				}
			});

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

	/**
	 * 元素通用字符串转换JSON
	 * 
	 * @param sourceObject
	 * @param key
	 * @param elementNode
	 */
	protected static void baseElementScript(JSONObject sourceObject, String key, String elementNode) {
		JSONArray selectFieldArray = sourceObject.getJSONArray(key);
		if (selectFieldArray != null && selectFieldArray.size() > 0) {
			JSONObject jsonObject = selectFieldArray.getJSONObject(0);
			if (jsonObject != null && !"[]".equals(jsonObject.toString())) {
				jsonObject.put(elementNode, (jsonObject.get(elementNode)).toString());
			} else {
				jsonObject.put(elementNode, null);
			}
		}
	}

	/**
	 * 转换元素action里面的字符串
	 * 
	 * @param jsonArray
	 */
	private static void actionJSON2Str(JSONArray jsonArray) {
		jsonArray.forEach(object -> {
			JSONObject jsonObject = (JSONObject) object;
			Set<String> keys = jsonObject.keySet();
			keys.forEach(key -> {
				switch (key) {
				case "tb_window_element_action_popwin":
				case "tb_window_element_action_service":
				case "tb_window_element_action_exesql":
				case "tb_window_element_action_js":
				case "tb_window_element_listener":
					baseAction2JsonScript(jsonObject, key);
					break;
				default:
					break;
				}
			});
		});
	}

	private static void baseAction2JsonScript(JSONObject object, String key) {
		JSONArray array = object.getJSONArray(key);
		if (array.size() > 0) {
			array.forEach(element -> {
				JSONObject popJsonObject = (JSONObject) element;
				// 判断有没有filter
				// Set<String> popKeySet = popJsonObject.keySet();
				switch (key) {
				case "tb_window_element_action_popwin":
					String[] popwins = { "filter", "script" };
					exchangeJSON2String(popwins, popJsonObject);

					break;
				case "tb_window_element_action_service":
					String[] services = { "urltitle", "script", "switchfilter", "urlparams", "headers" };
					exchangeJSON2String(services, popJsonObject);
					break;
				case "tb_window_element_action_exesql":
					String[] exesqls = { "switchfilter", "customevent" };
					exchangeJSON2String(exesqls, popJsonObject);
					break;
				case "tb_window_element_action_js":
					JSONObject jss = popJsonObject.getJSONObject("customevent");
					if (jss != null) {
						popJsonObject.put("customevent", jss.getString("js"));
					}
					break;
				}
			});
		}
	}
}
