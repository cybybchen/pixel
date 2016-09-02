package com.trans.pixel.controller.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
	    	   sb.append(buffer);
	       }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("input hegame recall error:" + e);
		}
	   
		logger.warn(sb.toString());
		String[] idfas;
		JSONObject json = new JSONObject();
		try {
			idfas = java.net.URLDecoder.decode(sb.toString(), "utf-8").substring(5).split(",");
			for (String idfa : idfas) {
				json.put(idfa, idfaService.queryIdfa(idfa));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return json.toString();
	}
	
	@RequestMapping("/idfa_login")
   @ResponseBody
	public String idfa_login(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = getParamsMap(request);
		logger.warn("recall params is:" + params);
		idfaService.addIdfa(params);
		return "{\"success\":true,\"message\":\"ok\"}";
	}
	
	private Map<String, String> getParamsMap(HttpServletRequest request) {
		Map<String, String> paramsMap = HmacSHA1Encryption.parseRequest(request);
		logger.info("params " + paramsMap);
		
		return paramsMap;
	}
}
