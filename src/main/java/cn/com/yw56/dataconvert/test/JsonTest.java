package cn.com.yw56.dataconvert.test;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class JsonTest {
	public static void main(String[] args) {
		Person person1 = new Person();
		Person person = new Person(1,"2");
		Person person2 = new Person(1,"2");
		Person person3 = new Person();
		Person person4 = new Person();
		System.out.println(person.getName());
		List<Person> list = new ArrayList<Person>();
		list.add(person3);
		list.add(person);
		list.add(person1);
		list.add(person2);
		list.add(person4);
		
		String string = JSON.toJSONString(list);
		System.out.println(string);
		// 去掉空对象
		string = string.replace(",{}"," ");
		string = string.replace("{},", " ");
		// 再次转换
		JSONArray jsonArray = JSONArray.parseArray(string);
		System.out.println(JSONArray.parseArray(string).size());
		System.out.println(string);
		

	}
}
