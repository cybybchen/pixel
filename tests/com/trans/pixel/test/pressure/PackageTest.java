package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestUsePropCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.UserProp;
import com.trans.pixel.test.BaseTest;

public class PackageTest extends BaseTest {
	private static Logger logger = Logger.getLogger(PackageTest.class);
	
	public void testPackage(RequestCommand.Builder req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req.build());
		RequestUsePropCommand.Builder b = RequestUsePropCommand.newBuilder();
		UserProp userProp = loginResponse.getUserPropCommand().getUserProp(0);
		b.setPropId(userProp.getPropId());
		b.setPropCount(userProp.getPropCount());
		builder.setUsePropCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
}
