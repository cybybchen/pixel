package com.trans.pixel.controller.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.trans.pixel.service.IdfaService;
import com.trans.pixel.utils.HmacSHA1Encryption;

@Controller
@Scope("prototype")
public class IdfaAction {
	private Logger logger = Logger.getLogger(IdfaAction.class);

	@Resource
	private IdfaService idfaService;
	
	@RequestMapping("/idfa_query")
   @ResponseBody
	public String idfa_query(HttpServletRequest request, HttpServletResponse response) {
		StringBuffer sb = new StringBuffer();
		//获取HTTP请求的输入流
		InputStream is;
	   try {
		   is = request.getInputStream();
		   String buffer = "";
	       //已HTTP请求输入流建立一个BufferedReader对象
	       BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
	       //读取HTTP请求内容
	       while ((buffer = br.readLine()) != null) {
	    	   //在页面中显示读取到的请求参数
	    	   sb.append(buffer+"\n");
	       }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("input hegame recall error:" + e);
		}
	   
      logger.warn(sb.toString());
      String[] idfas = sb.toString().split(",");
		JSONObject json = new JSONObject();
		for (String idfa : idfas) {
			json.put(idfa, idfaService.queryIdfa(idfa));
		}
		
		return json.toString();
	}
	
	@RequestMapping("/idfa_login")
   @ResponseBody
	public void idfa_login(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = getParamsMap(request);
		logger.warn("recall params is:" + params);
		idfaService.addIdfa(params);
	}
	
	private void idfaecall(String callback, String requestStr) {
		String urlStr = callback;
		logger.info("urlStr is:" + urlStr);
		InputStream is = null;
		
		URL url;
		HttpURLConnection conn = null;
		try {
			System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64)");
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5000);
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			
			conn.connect();
			int response = conn.getResponseCode();
			String line = "";
			if (response == 200) {
			
			} else{
			
			}
		} catch (MalformedURLException e) {
			
		} catch (IOException e) {
			
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}
	
	private Map<String, String> getParamsMap(HttpServletRequest request) {
		Map<String, String> paramsMap = HmacSHA1Encryption.parseRequest(request);
		logger.info("params " + paramsMap);
		
		return paramsMap;
	}
}
