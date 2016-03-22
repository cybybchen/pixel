package com.trans.pixel.service.redis;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.HeroEquipBean;
import com.trans.pixel.model.hero.HeroSkillBean;
import com.trans.pixel.model.hero.HeroStarBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ShopWillList;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.VipList;
import com.trans.pixel.utils.TypeTranslatedUtil;

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
