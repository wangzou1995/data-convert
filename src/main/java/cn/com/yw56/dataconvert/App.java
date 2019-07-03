package cn.com.yw56.dataconvert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alibaba.fastjson.JSONObject;

import cn.com.yw56.dataconvert.until.Ext2VueUntil;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/wangzou1995/Downloads/accountdetail_data.json"))));
		StringBuffer line = new StringBuffer();
	    String result = "";
		while ((result = reader.readLine()) != null) {
			line.append(result);
		}
		reader.close();
    	JSONObject jObject = JSONObject.parseObject(line.toString());
    	// System.out.println(jObject);
    	System.out.println(Ext2VueUntil.ext2VueData(jObject));
    }
}
