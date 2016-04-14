package com.trans.pixel.test;

import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestUsePropCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class PackageTest extends BaseTest {

	@Test
	public void testArea() {
		login();
		testPackage();
	}
	
	private void testPackage() {
		RequestUsePropCommand.Builder builder = RequestUsePropCommand.newBuilder();
		builder.setPropId(33009);
		builder.setPropCount(2);
        ResponseCommand response = request("usePropCommand", builder.build());
	}
}
