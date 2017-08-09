package com.trans.pixel.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class HmacSHA1Encryption {
	private static Logger logger = Logger.getLogger(HmacSHA1Encryption.class);
	private static final String MAC_NAME = "HmacSHA1";
	private static final String ENCODING = "UTF-8";
	public static final String JEWEL_SHA = "jewel_sha";
	
	public static Map<String, String> parseRequest(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		Map<String, String> paramsMap = new HashMap<String, String>();
		Iterator<Entry<String, String[]>> itr = params.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, String[]> entry = itr.next();
			paramsMap.put(entry.getKey(), entry.getValue()[0]);
		}
		
		return paramsMap;
	}
	
	public static Map<String, String[]> parseRequestForLog(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		Map<String, String[]> paramsMap = new HashMap<String, String[]>();
		Iterator<Entry<String, String[]>> itr = params.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, String[]> entry = itr.next();
			String [] values = {entry.getValue()[0]};
			paramsMap.put(entry.getKey(), values);
		}
		
		return paramsMap;
	}
	
	public static String getSortedRequestKeys(Map<String, String> params) {
		List<String> paramList = new ArrayList<String>();
		Iterator<Entry<String, String>> itr = params.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, String> entry = itr.next();
			if (!entry.getKey().trim().equals("sign") && !entry.getKey().trim().equals("sign_return") 
					&& !entry.getKey().trim().equals("signature")
					&& !entry.getValue().trim().equals(""))
				paramList.add(entry.getKey());
		}
		Collections.sort(paramList);
		StringBuffer sBuffer = new StringBuffer();
		for (String s : paramList) {
			sBuffer.append(params.get(s));
			sBuffer.append("#");
		}

		if (sBuffer.toString().length() == 0)
			return "";
		
		return sBuffer.toString().substring(0, sBuffer.toString().length() - 1);
	}
	
	public static String getMipaySortedRequestKeys(Map<String, String> params) {
		List<String> paramList = new ArrayList<String>();
		Iterator<Entry<String, String>> itr = params.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, String> entry = itr.next();
			if (!entry.getKey().trim().equals("signature")
					&& !entry.getValue().trim().equals(""))
				paramList.add(entry.getKey());
		}
		Collections.sort(paramList);
		StringBuffer sBuffer = new StringBuffer();
		for (String s : paramList) {
			sBuffer.append(s);
			sBuffer.append("=");
			sBuffer.append(params.get(s));
			sBuffer.append("&");
		}

		if (sBuffer.toString().length() == 0)
			return "";
		
		return sBuffer.toString().substring(0, sBuffer.toString().length() - 1);
	}
	
	public static String getSortedRequestStr(Map<String, String> params) {
		List<String> paramList = new ArrayList<String>();
		Iterator<Entry<String, String>> itr = params.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, String> entry = itr.next();
			paramList.add(entry.getKey() + "=" + entry.getValue());
		}
		Collections.sort(paramList);
		StringBuffer sBuffer = new StringBuffer();
		for (String s : paramList) {
			sBuffer.append(s);
			sBuffer.append("&");
		}

		return sBuffer.toString().substring(0, sBuffer.toString().length() - 1);
	}
	
	public static String getUnicomRequestStr(List<String> orderNameList, Map<String, String> params) {
		List<String> paramList = new ArrayList<String>();
		for (int i = 0;i < orderNameList.size(); ++i) {
			String orderName = orderNameList.get(i);
			paramList.add(orderName + "=" + params.get(orderName));
		}
//		Iterator<Entry<String, String>> itr = params.entrySet().iterator();
//		while(itr.hasNext()) {
//			Entry<String, String> entry = itr.next();
//			String key = entry.getKey();
//			if (!key.equals(XmlParseString.UNICOM_SIGNMSG)) {
//				paramList.add(entry.getKey() + "=" + entry.getValue());
//			}
//		}
		StringBuffer sBuffer = new StringBuffer();
		for (String s : paramList) {
			sBuffer.append(s);
			sBuffer.append("&");
		}

		logger.warn("unicom request str is:"+sBuffer.toString().substring(0, sBuffer.toString().length() - 1));
		return sBuffer.toString().substring(0, sBuffer.toString().length() - 1);
	}

	public static String getTelecomRequestStr(List<String> orderNameList, Map<String, String> params) {
		List<String> paramList = new ArrayList<String>();
		for (int i = 0;i < orderNameList.size(); ++i) {
			String orderName = orderNameList.get(i);
			String value = params.get(orderName);
			if (value != null && !value.equals(""))
				paramList.add(params.get(orderName));
		}
		StringBuffer sBuffer = new StringBuffer();
		for (String s : paramList) {
			sBuffer.append(s);
//			sBuffer.append("+");
		}

		logger.warn("telecom request str is:"+sBuffer.toString());
		return sBuffer.toString();
	}
	
	/**
	 * 使用HMAC-SHA1 签名方法对对encryptText进行签名
	 * 
	 * @param encryptText
	 *            被签名的字符串
	 * @param encryptKey
	 *            密钥
	 * @return 返回被加密后的字符串
	 * @throws Exception
	 */
	public static String HmacSHA1Encrypt(String encryptText, String encryptKey)
			throws Exception {
		byte[] data = encryptKey.getBytes(ENCODING);
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		// 生成一个指定Mac 算法的Mac 对象
		Mac mac = Mac.getInstance(MAC_NAME);
		// 用给定密钥初始化Mac 对象
		mac.init(secretKey);
		byte[] text = encryptText.getBytes(ENCODING);
		// 完成Mac 操作
		byte[] digest = mac.doFinal(text);
		
		StringBuilder sBuilder = bytesToHexString(digest);
		return sBuilder.toString().toUpperCase();
	}
	
	public static String HmacSHA1EncryptForMipay(String encryptText, String encryptKey)
			throws Exception {
		byte[] data = encryptKey.getBytes(ENCODING);
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		// 生成一个指定Mac 算法的Mac 对象
		Mac mac = Mac.getInstance(MAC_NAME);
		// 用给定密钥初始化Mac 对象
		mac.init(secretKey);
		byte[] text = encryptText.getBytes(ENCODING);
		// 完成Mac 操作
		byte[] digest = mac.doFinal(text);
		
		StringBuilder sBuilder = bytesToHexString(digest);
		return sBuilder.toString();
	}
	
	/**
	 * 转换成Hex
	 ** 
	 * @param bytesArray
	 */
	public static StringBuilder bytesToHexString(byte[] bytesArray) {
		if (bytesArray == null) {
			return null;
		}
		StringBuilder sBuilder = new StringBuilder();
		for (byte b : bytesArray) {
			String hv = String.format("%02x", b);
			sBuilder.append(hv);
		}
		return sBuilder;
	}
}
