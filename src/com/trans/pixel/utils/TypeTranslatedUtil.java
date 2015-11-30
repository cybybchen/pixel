package com.trans.pixel.utils;


public class TypeTranslatedUtil {
	public static int stringToInt(String str) {
		int id = 0;
		try {
			id = Integer.parseInt(str);
		} catch (Exception e) {

		}
		return id;
	}
	
	public static float stringToFloat(String str) {
		float id = 0.f;
		try {
			id = Float.parseFloat(str);
		} catch (Exception e) {

		}
		return id;
	}
	
	public static long stringToLong(String str) {
		long id = 0;
		try {
			id = Long.parseLong(str);
		} catch (Exception e) {

		}
		return id;
	}
	
	public static boolean stringToBoolean(String str) {
		boolean result = false;
		try {
			result = Boolean.parseBoolean(str);
		} catch (Exception e) {

		}
		return result;
	}
}
