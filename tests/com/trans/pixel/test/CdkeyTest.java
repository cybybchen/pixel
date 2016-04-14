package com.trans.pixel.test;

import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestCdkeyCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class CdkeyTest extends BaseTest {
	
	@Test
	public void test() {
		login();
		testCdkey();
	}
	
	public void testCdkey() {
		RequestCdkeyCommand.Builder builder = RequestCdkeyCommand.newBuilder();
		builder.setKey("aaaaaa");
        ResponseCommand response = request("cdkeyCommand", builder.build());
	}
}
