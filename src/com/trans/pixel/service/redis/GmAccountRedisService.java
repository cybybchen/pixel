package com.trans.pixel.service.redis;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.DirConst;

@Repository
public class GmAccountRedisService extends RedisService{
	
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

}
