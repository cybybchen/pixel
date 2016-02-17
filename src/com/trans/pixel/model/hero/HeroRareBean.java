package com.trans.pixel.model.hero;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroRareBean {
	private int rare = 0;
	private int level = 1;
	public int getRare() {
		return rare;
	}
	public void setRare(int rare) {
		this.rare = rare;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	public static List<HeroRareBean> xmlParse() {
		Logger logger = Logger.getLogger(HeroRareBean.class);
		List<HeroRareBean> list = new ArrayList<HeroRareBean>();
		String fileName = FILE_NAME;
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> heroList = root.elements();
			for (int i = 0; i < heroList.size(); i++) {
				HeroRareBean bean = new HeroRareBean();
				Element heroElement = (Element) heroList.get(i);
				bean.setRare(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(RARE)));
				bean.setLevel(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(LEVEL)));
				
				list.add(bean);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_herorare.xml";
	private static final String RARE = "rare";
	private static final String LEVEL = "level";
}
