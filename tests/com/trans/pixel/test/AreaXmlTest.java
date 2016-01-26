package com.trans.pixel.test;

import static org.junit.Assert.fail;

import org.dom4j.DocumentException;
import org.junit.Test;

import com.googlecode.protobuf.format.XmlFormat;
import com.googlecode.protobuf.format.XmlFormat.ParseException;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.RedisService;

public class AreaXmlTest extends BaseTest {

	@Test
	public void test() {
		buildXml();
	}
	
	public void buildXml() {
		AreaInfo area = AreaRedisService.buildArea(1);
		AreaMode.Builder areasBuilder = AreaMode.newBuilder();
		areasBuilder.addRegion(area);
		String xmlFormat = XmlFormat.printToString(areasBuilder.build());
		WriteToFile(xmlFormat, "area1.xml");
		
		String xml = ReadFromFile("area.xml");
		AreaMode.Builder builder = AreaMode.newBuilder();
		try {
			XmlFormat.merge(xml, builder);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Not yet implemented");
		}
		System.out.println(RedisService.formatJson(builder.build()));
	}

}
