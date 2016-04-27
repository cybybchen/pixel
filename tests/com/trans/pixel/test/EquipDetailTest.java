package com.trans.pixel.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import com.trans.pixel.service.redis.RedisService;

public class EquipDetailTest extends BaseTest {
	
	@Test
	public void test() {
		try{
			String xml = RedisService.ReadProperties("equip_detail.xml");
			Document doc = DocumentHelper.parseText(xml);
			List<Element> rootList = doc.getRootElement().element("database").elements("table");
			Map<String, String> map = new HashMap<String, String>();
			for (Element element : rootList) {
				String id = "";
				String origins = "";
				for(Element column : (List<Element>)element.elements()){
					if("id".equals(column.attribute("name").getValue()))
						id = column.getText();
					else if("origin".equals(column.attribute("name").getValue()))
						origins = column.getText();
				}
				for(String origin : origins.split(",")){
					String value = id;
					if(map.containsKey(origin)){
						value = map.get(origin);
						if(!value.contains(id))
							value +=","+id;
					}
					map.put(origin, value);
				}
			}
			System.out.println(map);
			for (Element element : rootList) {
				String id = "";
				for(Element column : (List<Element>)element.elements()){
					if("id".equals(column.attribute("name").getValue()))
						id = column.getText();
				}
				for(Element column : (List<Element>)element.elements()){
					if("target".equals(column.attribute("name").getValue()) && map.containsKey(id))
						column.setText(map.get(id));
				}
			}
			RedisService.WriteProperties(doc.asXML(), "equip_detail.xml");
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
