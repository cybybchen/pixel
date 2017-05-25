package com.trans.pixel.utils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class Common {
	public static Map<String, String> getParamsMap(HttpServletRequest request) {
		Map<String, String> paramsMap = HmacSHA1Encryption.parseRequest(request);
		
		return paramsMap;
	}
}
