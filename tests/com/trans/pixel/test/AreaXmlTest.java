package com.trans.pixel.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.googlecode.protobuf.format.XmlFormat;
import com.googlecode.protobuf.format.XmlFormat.ParseException;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.service.redis.AreaRedisService;

public class AreaXmlTest extends BaseTest {

	@Test
	public void test() {
		buildXml();
	}
	
	public void buildXml() {
		AreaInfo area = AreaRedisService.buildArea(1);
		String xmlFormat = XmlFormat.printToString(area);
		WriteToFile(xmlFormat, "area.xml");
		
		String xml = ReadFromFile("area.xml");
		AreaMode.Builder builder = AreaMode.newBuilder();
		try {
			XmlFormat.merge(xml, builder);
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Not yet implemented");
		}
		System.out.println(builder.build());
	}

}
