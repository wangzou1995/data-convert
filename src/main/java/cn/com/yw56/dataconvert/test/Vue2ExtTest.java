package cn.com.yw56.dataconvert.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.alibaba.fastjson.JSONObject;

import cn.com.yw56.dataconvert.until.Vue2ExtUntil;

public class Vue2ExtTest {
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
    			new FileInputStream(new File("/Users/wangzou1995/Downloads/test.json"))));
		StringBuffer line = new StringBuffer();
	    String result = "";
		while ((result = reader.readLine()) != null) {
			line.append(result);
		}
		reader.close();
    	JSONObject jObject = JSONObject.parseObject(line.toString());
    	JSONObject jsonObject = Vue2ExtUntil.vue2ExtData(jObject);
    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
    			new FileOutputStream(new File("/Users/wangzou1995/Downloads/test_ext.json"))));
    	writer.write(jsonObject.toString());
    	writer.flush();
    	writer.close();
	}
}
