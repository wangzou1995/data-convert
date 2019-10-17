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

import cn.com.yw56.dataconvert.until.Ext2VueUntil;

public class Ext2VueTest {
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
    			new FileInputStream(new File("/Users/wangzou1995/Downloads/account_consignor_edit_data.json"))));
		StringBuffer line = new StringBuffer();
	    String result = "";
		while ((result = reader.readLine()) != null) {
			line.append(result);
		}
		reader.close();
    	JSONObject jObject = JSONObject.parseObject(line.toString());
    	JSONObject jsonObject = Ext2VueUntil.ext2VueData(jObject);
    	
    	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
    			new FileOutputStream(new File("/Users/wangzou1995/Downloads/account_consignor_edit_data_vue.json"))));
    	writer.write(jsonObject.toString());
    	writer.flush();
    	writer.close();
	}
}
