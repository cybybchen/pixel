package com.trans.pixel.service.redis;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.constants.RedisKey;

@Repository
public class GmAccountRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(GmAccountRedisService.class);
	
	public Map<String, String> getGmAccounts() {
		Map<String, String> map = new HashMap<String, String>();
		try {
			String filePath = DirConst.getConfigXmlPath("gm_account.xml");
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> list = root.elements();
			for (int i = 0; i < list.size(); i++) {
				Element element = (Element) list.get(i);
				map.put(element.attributeValue("account"), element.attributeValue("password"));
			}
		} catch (Exception e) {
			logger.error("parse gm_account failed");
		}
		return map;
	}
	
	public String getSession(String account){
		String session = get(RedisKey.GM_SESSION_PREFIX+account);
		if(session != null){
			expire(RedisKey.GM_SESSION_PREFIX+account, 24*3600*1000);
			expire(RedisKey.GM_SESSION_PREFIX+session, 24*3600*1000);
		}
		return session;
	}

	public String createNewSession(String account){
		String session = DigestUtils.md5Hex(account+System.currentTimeMillis());
		set(RedisKey.GM_SESSION_PREFIX+account, session);
		expire(RedisKey.GM_SESSION_PREFIX+account, 24*3600*1000);
		set(RedisKey.GM_SESSION_PREFIX+session, account);
		expire(RedisKey.GM_SESSION_PREFIX+session, 24*3600*1000);
		return session;
	}
	
	public String loginGmAccount(String account, String password){
		Map<String, String> map = getGmAccounts();
	   for(Map.Entry<String, String> entry : map.entrySet()){
		   if(entry.getKey().equals(account) && entry.getValue().equals(password)){
			   String session = getSession(account);
			   if(session == null)
				   session = createNewSession(account);
			   return session;
		   }
	   }
	   return null;
	}

}
