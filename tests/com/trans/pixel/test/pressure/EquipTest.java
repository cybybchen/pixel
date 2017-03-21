package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.EquipProto.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.EquipProto.UserEquip;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.test.BaseTest;

public class EquipTest extends BaseTest {
	private static Logger logger = Logger.getLogger(EquipTest.class);
	
	public void testFenjieEquip(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestFenjieEquipCommand.Builder b = RequestFenjieEquipCommand.newBuilder();
		UserEquip userEquip = loginResponse.getUserEquipCommand().getUserEquip(0);
		b.setEquipId(userEquip.getEquipId());
		b.setEquipCount(userEquip.getEquipCount());
		builder.setFenjieEquipCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
