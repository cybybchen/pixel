package com.trans.pixel.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.googlecode.protobuf.format.XmlFormat;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.RedisService;

public class AreaXmlTest extends BaseTest {

	@Test
	public void test() {
		buildXml();
	}
	
	public void buildXml() {
		AreaMode.Builder areasBuilder = AreaMode.newBuilder();
		areasBuilder.mergeFrom(new AreaRedisService().buildAreaMode());
		String xmlFormat = XmlFormat.printToString(areasBuilder.build());
		RedisService.WriteProperties(xmlFormat, "area1.xml");
		System.out.println(RedisService.formatJson(areasBuilder.build()));
		
		String xml = RedisService.ReadProperties("area.xml");
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
