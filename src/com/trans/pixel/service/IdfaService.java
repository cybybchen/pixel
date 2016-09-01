package com.trans.pixel.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.IdfaBean;
import com.trans.pixel.model.mapper.IdfaMapper;

@Service
public class IdfaService {

	@Resource
	private IdfaMapper idfaMapper;
	public void addIdfa(Map<String, String> params) {
		IdfaBean idfa = initIdfa(params);
		idfaMapper.addIdfa(idfa);
	}
	
	public int queryIdfa(String idfa) {
		Object ret = idfaMapper.queryIdfa(idfa);
		if (ret == null)
			return 0;
		else 
			return 1;
	}
	
	public void updateIdfaStatus(String idfa) {
		Object ret = idfaMapper.queryIdfa(idfa);
		if (ret == null)
			return;
		
		IdfaBean idfaBean = (IdfaBean) ret;
		if (idfaBean.getIsRecall() == 0) {
			idfaBean.setIsRecall(1);
			idfaMapper.updateStatus(idfaBean);
			idfaecall(idfaBean.getCallback());
		}
			
		
	}
	
	private IdfaBean initIdfa(Map<String, String> params) {
		IdfaBean idfa = new IdfaBean();
		idfa.setIdfa(params.get("idfa"));
		idfa.setIp(params.get("ip"));
		idfa.setCallback(params.get("callback"));
		
		return idfa;
	}

	
	private void idfaecall(String callback) {
		String urlStr = callback;
		// logger.info("urlStr is:" + urlStr);
//		InputStream is = null;
		
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
//			String line = "";
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
}
