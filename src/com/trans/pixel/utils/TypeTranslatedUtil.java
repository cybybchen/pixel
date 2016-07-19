package com.trans.pixel.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


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
	
	public static JSONArray jsonGetArray(JSONObject json, String property) {
		JSONArray value = null;
		try {
			value = json.getJSONArray(property);
		} catch (JSONException e) {

		}
		return value != null ? value : new JSONArray();
	}
	
	public static int jsonGetInt(JSONObject json, String property) {
		int value = 0;
		try {
			value = json.getInt(property);
		} catch (JSONException e) {

		}
		return value;
	}
	
	public static long jsonGetLong(JSONObject json, String property) {
		long value = 0;
		try {
			value = json.getLong(property);
		} catch (JSONException e) {

		}
		return value;
	}
	
	public static float jsonGetFloat(JSONObject json, String property) {
		float value = .0f;
		try {
			value = (float) json.getDouble(property);
		} catch (JSONException e) {

		}
		return value;
	}
	
	public static String jsonGetString(JSONObject json, String property) {
		String value = "";
		try {
			value = json.getString(property);
		} catch (JSONException e) {
			value = "";
		}
		return value;
	}  
	
	public static boolean jsonGetBoolean(JSONObject json, String property) {
		boolean value = false;
		try {
			value = json.getBoolean(property);
		} catch (JSONException e) {

		}
		return value;
	} 
}
