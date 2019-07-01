package cn.com.yw56.dataconvert.until;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 数据转换工具类
 * 
 * @author wangzou
 *
 */
public class Ext2VueUntil {
	private static final ThreadLocal<JSONObject> threadLocal = new ThreadLocal<>();
	/**
	 * EXT转换VUE
	 * 
	 * @param jsonObject
	 * @return
	 */
	public static JSONObject ext2VueData(JSONObject jsonObject) {
		JSONObject resultObj = new JSONObject();
		// 获取窗口配置
		JSONArray windows = jsonObject.getJSONArray("tb_window");
		// 填写窗口信息
		resultObj = initWindow(windows.getJSONObject(0));
		return resultObj;
	}

	/**
	 * 初始化窗口数据
	 * 
	 * @param jsonObject
	 * @return
	 */
	protected static JSONObject initWindow(JSONObject jsonObject) {
		JSONObject result = new JSONObject();
		Set<String> keys = jsonObject.keySet();
		for (String string : keys) {
			if ("tb_window_layout".equals(string)) {
				result.put(string, initContainerLaout(resetSort(jsonObject.getJSONArray(string), "container")));
			} else {
				result.put(string, jsonObject.get(string));
			}
		}
		return result;
	}

	/**
	 * 初始化容器配置
	 * 
	 * @param jsonArray
	 * @return
	 */
	protected static JSONArray initContainerLaout(JSONArray jsonArray) {
		JSONArray result = new JSONArray();

		jsonArray.forEach((object) -> {
			// 容器
			JSONObject sourceObj = (JSONObject) object;
			// 占位
			JSONObject temp = new JSONObject();
			// 获取容器类型
			String containerType = sourceObj.getString("containertype");
			if (containerType.equals("grid")) {
				temp.put("tb_tool_element", new JSONArray());
				temp.put("isShowTool", true);
				temp.put("toolPosition", "bottom");
			}
			// 获取容器id
			int containerId = sourceObj.getIntValue("id");
			// 存放type
			temp.put("type", containerType);
			// 初始化容器

			initContainerBasic(sourceObj, containerType, containerId, temp);
			// 初始化子容器
			// 查询点在哪里 result
			JSONObject parent = findParentById(result, sourceObj.getInteger("parentid"));
			if (parent != null) {
				if (containerType.equals("toolbar")) {
					parent.getJSONArray("tb_tool_element").add(temp);
				} else {
					parent.getJSONArray("tb_window_element").add(temp);
				}
			} else {
				result.add(temp);

			}
		});
		return result;
	}

	/**
	 * 通用代码
	 * 
	 * @param jsonObject
	 * @param containerType
	 * @param containerId
	 * @param temp
	 */
	private static void initContainerBasic(JSONObject jsonObject, String containerType, int containerId,
			JSONObject temp) {
		Set<String> keys = jsonObject.keySet();
		keys.forEach(key -> {
			if (!"tb_window_element".equals(key)) {
				temp.put(key, jsonObject.get(key));
			} else {
				JSONArray jsonArray = jsonObject.getJSONArray(key);
				if (jsonArray.size() > 0) {
					temp.put(key, initElement(containerType,
							resetSort(resetSort(jsonObject.getJSONArray(key), "row"), "colum"), containerId));
				} else {
					temp.put(key, jsonArray);
				}
			}
		});
	}

	/**
	 * 初始化元素信息
	 * 
	 * @param containerType
	 * @param array
	 * @param containerId
	 * @return
	 */
	protected static JSONArray initElement(String containerType, JSONArray array, int containerId) {
		// 添加行元素
		// 存放row 的关系
		Map<Integer, Integer> rowLocationMap = new HashMap<>();
		JSONArray jsonArray = new JSONArray();
		if (array.size() > 0) {
			switch (containerType) {
			case "searchform":
			case "form":
				// 获取元素
				for (Object obj : array) {
					JSONObject elementObj = (JSONObject) obj;
					int temp = elementObj.getInteger("rowid");
						if (!rowLocationMap.containsKey(temp)) {
							jsonArray.add(getRow(containerId));
						rowLocationMap.put(temp, jsonArray.size() - 1);
						}
					JSONObject rowObj = jsonArray.getJSONObject(rowLocationMap.get(temp));
					setCol(rowObj.getJSONArray("row"), elementObj);
				}
				break;
			case "grid":
				for (Object obj : array) {
					JSONObject elementObj = (JSONObject) obj;
					elementObj.put("parenttype", "grid");
					elementObj.put("parentid", containerId);
					jsonArray.add(elementObj);
				}
				break;
			default:
				jsonArray = array;
			}
		} else {

		}
		return jsonArray;
	}

	/**
	 * 获取行元素
	 * 
	 * @param parentid
	 * @return
	 */
	protected static JSONObject getRow(int parentid) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("row", new JSONArray());
		jsonObject.put("name", "行元素");
		jsonObject.put("type", "row");
		jsonObject.put("id", IdSequenceUtils.getId());
		jsonObject.put("icon", "iconfont iconrow");
		jsonObject.put("parentid", parentid);
		return jsonObject;
	}

	/**
	 * 
	 * 设置单元格
	 * 
	 * @param target
	 * @param source
	 */
	protected static void setCol(JSONArray target, JSONObject source) {
		JSONArray jsonArray = new JSONArray();
		JSONObject jObject = new JSONObject();
		// 字符串处理
		// str2Json(source);
		jsonArray.add(source);
		jObject.put("col", jsonArray);
		target.add(jObject);
	}

	/**
	 * 找父容器
	 * 
	 * @param source
	 * @param parentid
	 * @return
	 */
	protected static JSONObject findParentById(Object source, int parentid) {
		if (source instanceof JSONObject) {
			JSONObject sJsObject = (JSONObject) source;
			if (sJsObject.containsKey("id")) {
				if (sJsObject.getInteger("id") == parentid) {
					threadLocal.set(sJsObject);
				} else {
					Set<String> keys = sJsObject.keySet();
					keys.forEach(key -> {
						if (sJsObject.get(key) instanceof JSONArray) {
							findParentById(sJsObject.getJSONArray(key), parentid);
						}
					});
				}
			}
		} else {
			JSONArray jsonArray = (JSONArray) source;
			if (jsonArray.size() > 0) {
				for (int i = 0; i < jsonArray.size(); i++) {
					findParentById(jsonArray.getJSONObject(i), parentid);
				}
			}
		}
		return threadLocal.get();
	}

	/**
	 * 获取子对象
	 * 
	 * @param array
	 * @param id
	 * @return
	 */
	protected static JSONArray findChildById(JSONArray array, int id) {
		JSONArray result = new JSONArray();
		for (int i = 0; i < array.size(); i++) {
			if (array.getJSONObject(i).getInteger("parentid") == id) {
				result.add(array.getJSONObject(i));
			}
		}
		return result;
	}

	/**
	 * 判断是否为数组
	 * 
	 * @param value
	 * @return
	 */
	protected static boolean isJSONArray(String value) {
		Object object = JSON.parse(value);
		return object instanceof JSONArray;
	}

	/**
	 * 遇见工具类型重新查询
	 * 
	 * @param jsonObject
	 * @param parentid
	 */
	protected static void resetFind(JSONObject jsonObject, int parentid) {
		Set<String> keys1 = jsonObject.keySet();
		keys1.forEach(key -> {
			Object object = jsonObject.get(key);
			if (object instanceof JSONArray || object instanceof JSONObject) {
				findParentById(object, parentid);
			}
		});
	}

	/**
	 * 排序
	 * 
	 * @param jsonArray
	 * @param type
	 * @return
	 */
	protected static JSONArray resetSort(JSONArray jsonArray, String type) {
		List<JSONObject> list = JSONArray.parseArray(jsonArray.toJSONString(), JSONObject.class);
		Collections.sort(list, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				int a = o1.getInteger(type == "container" ? "orderid" : type == "row" ? "rowid" : "columnid");
				int b = o2.getInteger(type == "container" ? "orderid" : type == "row" ? "rowid" : "columnid");
				if (a > b) {
					return 1;
				} else if (a == b) {
					return 0;
				} else
					return -1;
			}
		});
		JSONArray result = JSONArray.parseArray(list.toString());
		return type == "container" ? resetSortByParentId(result) : result;
	}

	/**
	 * 排序 （父id） -- 只针对容器进行排序
	 * 
	 * @param jsonArray
	 * @return
	 */
	protected static JSONArray resetSortByParentId(JSONArray jsonArray) {
		List<JSONObject> list = JSONArray.parseArray(jsonArray.toJSONString(), JSONObject.class);
		Collections.sort(list, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				int a = o1.getInteger("parentid");
				int b = o2.getInteger("parentid");
				if (a > b) {
					return 1;
				} else if (a == b) {
					return 0;
				} else
					return -1;
			}
		});
		// 将toolbar 放置最后
		Collections.sort(list, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				// TODO Auto-generated method stub
				int a = o1.getString("containertype").equals("toolbar") ? 1 : 0;
				int b = o2.getString("containertype").equals("toolbar") ? 1 : 0;
				if (a > b) {
					return 1;
				} else if (a == b) {
					return 0;
				} else
					return -1;
			}
		});
		JSONArray result = JSONArray.parseArray(list.toString());
		return result;
	}
	protected static void str2Json (JSONObject sObject) {
		Set<String> keys = sObject.keySet();
		keys.forEach(k -> {
			switch (k) {
			case "tb_window_selectfields":	
				JSONArray selectFieldArray = sObject.getJSONArray(k);
				if (selectFieldArray.size() > 0) {
				JSONObject jsonObject = selectFieldArray.getJSONObject(0);
				jsonObject.put("datasource", JSONObject.parse(jsonObject.getString("datasource")));
				}
				break;
			case "tb_window_element_action":
				JSONArray listenerArray = sObject.getJSONArray(k);
				if (listenerArray.size() > 0) {
					listenerArray.forEach(json -> {
						System.out.println(((JSONObject) json).getString("jsscript"));
					((JSONObject) json).put("jsscript", JSONObject.parse(((JSONObject) json).getString("jsscript")));
				});
				}
				break;
			default:
				break;
			}
		});
	}
}
