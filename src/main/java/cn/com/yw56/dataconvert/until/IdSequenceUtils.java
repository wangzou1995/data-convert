package cn.com.yw56.dataconvert.until;

import java.util.UUID;

public class IdSequenceUtils {
	public  static  Long getId() {
		return (long) UUID.randomUUID().toString().hashCode();
	}
}
