package cn.com.yw56.dataconvert.until;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * EXT数据转换工具类
 * 
 * @author wangzou
 *
 */
public class Ext2VueUntil {
	private static final ThreadLocal<JSONObject> threadLocal = new ThreadLocal<>();
	private static final ThreadLocal<JSONArray> messageThreadLocal = new ThreadLocal<>();
	private static String MESSAGE_STR = "messagetransid";
	private static String MESSAGE_NODE = "tb_message_template_trans";

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
		messageThreadLocal.set(jsonObject.getJSONArray(MESSAGE_NODE));
		resultObj.put("tb_window", initWindow(windows.getJSONObject(0)));
		resultObj.put("tb_message_template_trans" ,JSONArray.parseArray( jsonObject.getJSONArray(MESSAGE_NODE).toJSONString()));
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
		// 判断是否有过滤条件
		elementHaveTargetArray(result, keys, "filter");
		return result;
	}

	/**
	 * 初始化容器配置
	 * 
	 * @param jsonArray
	 * @return7
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
				// 只要不是末及 就有工具栏
				temp.put("isShowTool", !sourceObj.getBooleanValue("leaf"));
				temp.put("tb_tool_element", new JSONArray());
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
//			System.out.println(sourceObj.toString());
			int parentid = sourceObj.getInteger("parentid");
			JSONObject parent = parentid == -1 ? null : findParentById(result, parentid);

			if (parent != null) {
				String parentContainerTypeString = parent.getString("containertype");
				if ("grid".equals(parentContainerTypeString)) {
					parent.getJSONArray("tb_tool_element").add(temp);
					// 排序

				} else {
					parent.getJSONArray("tb_window_element").add(temp);
					parent.put("tb_window_element", resetSort(parent.getJSONArray("tb_window_element"), "containers"));
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
					temp.put(key,
							initElement(containerType,
									resetSort(resetSort(jsonObject.getJSONArray(key), "row"), "colum"), containerId,
									jsonObject));
				} else {
					temp.put(key, jsonArray);
				}
			}
		});
		// 判断是否有过滤字段
		elementHaveTargetArray(temp, keys, "filter");
		// 判断是否有自定义sql
		elementHaveTargetObject(temp, keys, "filtersql");
	}

	/**
	 * 初始化元素信息
	 * 
	 * @param containerType 容器类型
	 * @param array         元素集合
	 * @param containerId   容器id
	 * @return
	 */
	protected static JSONArray initElement(String containerType, JSONArray array, int containerId,
			JSONObject container) {
		// 添加行元素
		// 存放row 的关系
		// 定义row
		Map<Integer, Integer> rowLocationMap = new HashMap<>();
		// 定义group
		Map<Integer, JSONObject> groupMap = new HashMap<>();
		JSONArray jsonArray = new JSONArray();
		// 获取容器布局
		String layouttype = container.getString("layouttype");
		if (array.size() > 0) {
			switch (containerType) {
			case "searchform":
			case "form":
				// 获取元素
				// 获取元素 创建 row , list, group
				for (int i = 0; i < array.size(); i++) {
					// 判断
					JSONObject elementObj = (JSONObject) array.getJSONObject(i);
					int groupid = elementObj.getIntValue("groupid");
					if (groupid == 0) {
						int temp = elementObj.getInteger("rowid");
						if (!rowLocationMap.containsKey(temp)) {
							jsonArray.add(getRow(containerId,
									layouttype == null || layouttype.equals("hbox") ? "row" : "list"));
							rowLocationMap.put(temp, jsonArray.size() - 1);
						}
						JSONObject rowObj = jsonArray.getJSONObject(rowLocationMap.get(temp));
						setCol(rowObj.getJSONArray("row"), elementObj);
					} else {
						if (groupMap.containsKey(groupid)) {
							groupMap.get(groupid).getJSONArray("tb_window_element").add(elementObj);
						} else {
							JSONObject groupObject = createGroupContainer(container.getIntValue("id"),
									findGroupName(container, groupid), groupid, findGroupLayout(container, groupid));
							groupObject.getJSONArray("tb_window_element").add(elementObj);
							groupMap.put(groupid, groupObject);

						}
					}
				}
				groupMap.forEach((key, value) -> {
					Map<Integer, Integer> groupRowMap = new HashMap<>();
					JSONArray groupElements = value.getJSONArray("tb_window_element");
					String groupLayout = value.getString("layouttype");
					JSONArray groupArray = new JSONArray();
					groupElements.forEach(element -> {
						int temp = ((JSONObject) element).getInteger("rowid");
						if (!groupRowMap.containsKey(temp)) {
							groupArray.add(getRow(containerId,
									groupLayout == null || groupLayout.equals("hbox") ? "row" : "list"));
							groupRowMap.put(temp, groupArray.size() - 1);
						}
						JSONObject rowObj = groupArray.getJSONObject(groupRowMap.get(temp));
						setCol(rowObj.getJSONArray("row"), (JSONObject) element);
					});
					// 排序

					value.put("tb_window_element", groupArray);
					jsonArray.add(value);
				});
//				// 排序
//				JSONArray sortArray = resortRLG(jsonArray);
//				jsonArray.clear();
//				jsonArray.addAll(sortArray);
				break;
			default:
				for (Object obj : array) {
					JSONObject elementObj = (JSONObject) obj;
					if (containerType.equals("grid") || containerType.equals("tree")) {
						elementObj.put("parenttype", containerType);
						elementObj.put("parentid", containerId);
					}
					elementStr2Json(elementObj);
					jsonArray.add(elementObj);
				}
			}
		} else {

		}
		// 排序
		jsonArray.sort(new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				JSONObject jo1 = (JSONObject) o1;
				JSONObject jo2 = (JSONObject) o2;
				try {
					JSONArray ja1 = jo1.getJSONArray("row");
					JSONArray ja2 = jo2.getJSONArray("row");
					if (ja1.getJSONObject(0).getJSONArray("col").getJSONObject(0).getIntValue("rowid") > ja2
							.getJSONObject(0).getJSONArray("col").getJSONObject(0).getIntValue("rowid")) {
						return 1;
					} else {
						return -1;
					}
				} catch (Exception e) {
					return 0;
				}
			}
		});
		return jsonArray;
	}

	@SuppressWarnings("unused")
	private static JSONArray resortRLG(JSONArray jsonArray) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<Integer, JSONObject> rlgMap = new TreeMap(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		jsonArray.forEach(e -> {
			JSONObject element = (JSONObject) e;
			String type = element.getString("type");
			if (type.equals("row")) {
				rlgMap.put(element.getJSONArray("row").getJSONObject(0).getJSONArray("col").getJSONObject(0)
						.getIntValue("rowid"), element);
			} else if (type.equals("list")) {
				rlgMap.put(element.getJSONArray("row").getJSONObject(0).getJSONArray("col").getJSONObject(0)
						.getIntValue("colunmid"), element);
			} else {
				rlgMap.put(element.getJSONArray("tb_window_element").getJSONObject(0).getJSONArray("row")
						.getJSONObject(0).getJSONArray("col").getJSONObject(0).getIntValue("rowid"), element);
			}
		});
		JSONArray result = new JSONArray();
		rlgMap.forEach((key, value) -> {
			result.add(value);
		});
		return result;
	}

	private static String findGroupName(JSONObject container, int groupid) {
		JSONArray array = container.getJSONArray("tb_window_layout_field_group");
		for (int i = 0; i < array.size(); i++) {
			if (array.getJSONObject(i).getIntValue("id") == groupid) {
				return array.getJSONObject(i).getString("grouptitle");
			}
		}
		return null;
	}

	private static String findGroupLayout(JSONObject container, int groupid) {
		JSONArray array = container.getJSONArray("tb_window_layout_field_group");
		for (int i = 0; i < array.size(); i++) {
			if (array.getJSONObject(i).getIntValue("id") == groupid) {
				return array.getJSONObject(i).getString("layouttype");
			}
		}
		return null;
	}

	/**
	 * 获取行元素
	 * 
	 * @param parentid
	 * @return
	 */
	protected static JSONObject getRow(int parentid, String type) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("row", new JSONArray());
		jsonObject.put("name", "行元素");
		jsonObject.put("type", type);
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
		elementStr2Json(source);
		source.put("columnid", target.size());
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
				int a = o1.getInteger(
						type == "container" || type == "containers" ? "orderid" : type == "row" ? "rowid" : "columnid");
				int b = o2.getInteger(
						type == "container" || type == "containers" ? "orderid" : type == "row" ? "rowid" : "columnid");
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
				if (a < -1) {
					return 1;
				} else if (a > b) {
					return 1;
				} else if (a == b) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		// 将toolbar 放置最后
//		Collections.sort(list, new Comparator<JSONObject>() {
//			@Override
//			public int compare(JSONObject o1, JSONObject o2) {
//				// TODO Auto-generated method stub
//				int a = "toolbar".equals(o1.getString("containertype")) ? 1 : 0;
//				int b = "toolbar".equals(o2.getString("containertype")) ? 1 : 0;
//				if (a > b) {
//					return 1;
//				} else if (a == b) {
//					return 0;
//				} else
//					return -1;
//			}
//		});
		JSONArray result = JSONArray.parseArray(list.toString());
		return result;
	}

	/**
	 * 元素字符串转json
	 * 
	 * @param sObject
	 */
	protected static void elementStr2Json(JSONObject sObject) {
		Set<String> keys = sObject.keySet();
		keys.forEach(k -> {
			switch (k) {
			case "tb_window_selectfields":
				baseElementScript(sObject, k, "datasource");
				break;
			case "tb_window_element_action":
				actionStr2Json(sObject.getJSONArray(k));
				break;
			case "tb_window_formatsearch":
				baseElementScript(sObject, k, "script");
				break;
			case "tb_window_inputfields":
				JSONArray inpitsArray = sObject.getJSONArray(k);
				baseElementScript(sObject, k, "filter");
				if (inpitsArray != null && inpitsArray.size() > 0) {
					inpitsArray.forEach(obj -> {
						JSONObject inputObject = (JSONObject) obj;
						elementHaveTargetObject(inputObject, inputObject.keySet(), "switchfilter");
					});
				}
				break;
			case "visible":
			case "elementstatus":
				sObject.put(k, JSONObject.parse(sObject.getString(k)));
				break;
			default:
				break;
			}
		});
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
			selectFieldArray.forEach(obj -> {
				JSONObject jsonObject = (JSONObject) obj;
				try {
					Object parseObject = JSONObject.parse(jsonObject.getString(elementNode));
					jsonObject.put(elementNode, parseObject);
				} catch (Exception e) {

				}
			});

		}
	}

	/**
	 * 转换元素action里面的字符串
	 * 
	 * @param jsonArray
	 */
	private static void actionStr2Json(JSONArray jsonArray) {
		jsonArray.forEach(object -> {
			JSONObject jsonObject = (JSONObject) object;
			// 判断是否有export事件
			Set<String> keys = jsonObject.keySet();
			if (!keys.contains("tb_window_element_action_export")) {
				jsonObject.put("tb_window_element_action_export", new JSONArray());
			}
			keys.forEach(key -> {
				switch (key) {
				case "tb_window_element_action_popwin":
				case "tb_window_element_action_service":
				case "tb_window_element_action_exesql":
				case "tb_window_element_action_js":
				case "tb_window_element_action_export":
				case "tb_window_element_action_import":
				case "tb_window_element_action_updownload":
				case "tb_window_element_action_save":
				case "tb_window_element_action_cuobj":
					baseAction2JsonScript(jsonObject, key);
					break;
				default:
					break;
				}
			});
			elementHaveTargetArray(jsonObject, keys, "filter");
		});
	}

	private static void baseAction2JsonScript(JSONObject object, String key) {
		JSONArray array = object.getJSONArray(key);
		if (array.size() > 0) {
			array.forEach(element -> {
				JSONObject popJsonObject = (JSONObject) element;
				// 判断有没有filter
				Set<String> popKeySet = popJsonObject.keySet();
				switch (key) {
				case "tb_window_element_action_popwin":
					elementHaveTargetArray(popJsonObject, popKeySet, "filter");
					elementHaveTargetArray(popJsonObject, popKeySet, "script");
					break;
				case "tb_window_element_action_service":
					// 多做一件事情 添加messagetransid对应的数据
						int messageTransId = popJsonObject.getIntValue(MESSAGE_STR);
						if (messageTransId != 0) {
							//  查找存放
							popJsonObject.put(MESSAGE_NODE, getMessageObjById(messageTransId));
						}
					
					elementHaveTargetArray(popJsonObject, popKeySet, "script");
					elementHaveTargetArray(popJsonObject, popKeySet, "switchfilter");
					break;
				case "tb_window_element_action_exesql":
					elementHaveTargetArray(popJsonObject, popKeySet, "switchfilter");
					elementHaveTargetArray(popJsonObject, popKeySet, "customevent");
					break;
				case "tb_window_element_action_js":
					elementHaveTargetObject(popJsonObject, popKeySet, "customevent");
					break;
				default:
					break;
				}
			});
		}
	}

	/**
	 * 是否存在该节点 如果不存在 初始化节点数组
	 * 
	 * @param object
	 * @param keys
	 * @targetKey 目标节点
	 */
	protected static void elementHaveTargetArray(JSONObject object, Set<String> keys, String targetKey) {
		// 判断是否有过滤字段
		if (!keys.contains(targetKey)) {
			object.put(targetKey, new JSONArray());
		} else {
			String tempString = object.getString(targetKey);
			try {
				JSONArray array = JSONArray.parseArray(tempString);
				object.put(targetKey, object.getString(targetKey) == null ? new JSONArray() : array);
			} catch (JSONException e) {
				JSONArray array = new JSONArray();
				JSONObject createJson = new JSONObject();
				createJson.put("type", "js");
				createJson.put("js", tempString);
				array.add(createJson);
				object.put(targetKey, array);
			}

		}
	}

	/**
	 * 是否存在该节点 如果不存在 初始化节点对象
	 * 
	 * @param object
	 * @param keys
	 * @targetKey 目标节点
	 */
	protected static void elementHaveTargetObject(JSONObject object, Set<String> keys, String targetKey) {
		// 判断是否有自定义sql
		if (!keys.contains(targetKey)) {
			object.put(targetKey, new JSONObject());
		} else {

			String tempString = object.getString(targetKey);
			try {
				JSONObject tempObj = JSONObject.parseObject(tempString);
				object.put(targetKey, tempObj == null ? new JSONObject() : tempObj);
			} catch (JSONException e) {

				JSONObject createJson = new JSONObject();
				createJson.put("type", "js");
				createJson.put("js", tempString);
				object.put(targetKey, createJson);
			}
		}
	}

	/**
	 * 创建分组容器
	 * 
	 * @param parentid
	 * @param groupName
	 * @return
	 */
	protected static JSONObject createGroupContainer(int parentid, String groupName, int groupid, String layouttype) {
		JSONObject result = new JSONObject();
		result.put("type", "group");
		result.put("containertype", "group");
		result.put("containerdesc", groupName);
		result.put("parentid", parentid);
		result.put("id", groupid);
		result.put("tb_window_element", new JSONArray());
		result.put("orderid", 99999999);
		result.put("layouttype", layouttype);
		return result;
	}

	/**
	 * 获取元素index
	 * 
	 * @param elements 元素集合
	 * @param id       元素id
	 * @return 元素index
	 */
	protected static int getELementsIndex(JSONArray elements, int id) {
		for (int j = 0; j < elements.size(); j++) {
			if (elements.getJSONObject(j).getIntValue("id") == id) {
				return j;
			}
		}
		return -1;
	}
	/**
	 * 通过id查找报文转换配置
	 * @param id 报文转换id
	 * @return 报文转换对象
	 */
	protected static JSONObject getMessageObjById (int id) {
		JSONArray messageTransConfigsArray = messageThreadLocal.get();
		for (int i = 0; i < messageTransConfigsArray.size(); i++) {
			JSONObject messageObject = messageTransConfigsArray.getJSONObject(i);
			if (messageObject.getIntValue("id") == id) {
				messageTransConfigsArray.remove(i);
				return messageObject;
			}
		}
		return null;
	}
}
