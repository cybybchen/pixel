package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.trans.pixel.protoc.Commands.FenjieHeroInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.UserHero;
import com.trans.pixel.test.BaseTest;

public class HeroTest extends BaseTest {
	private static Logger logger = Logger.getLogger(HeroTest.class);
	
	public void testHeroLevelUpTest(RequestCommand.Builder req, ResponseCommand loginResponse, int type) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req.build());
		RequestHeroLevelUpCommand.Builder b = RequestHeroLevelUpCommand.newBuilder();
		UserHero userHero = loginResponse.getGetUserHeroCommand().getUserHero(0);
		b.setHeroId(userHero.getHeroId());
		b.setInfoId(userHero.getHeroInfo(0).getInfoId());
		b.setLevelUpType(type);
		builder.setHeroLevelUpCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
	public void testFenjieHeroTest(RequestCommand.Builder req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req.build());
		RequestFenjieHeroCommand.Builder b = RequestFenjieHeroCommand.newBuilder();
		UserHero userHero = loginResponse.getGetUserHeroCommand().getUserHero(0);
		FenjieHeroInfo.Builder fenjieHero = FenjieHeroInfo.newBuilder();
		fenjieHero.setHeroId(userHero.getHeroId());
		fenjieHero.setInfoId(userHero.getHeroInfo(0).getInfoId());
		b.addFenjieHero(fenjieHero.build());
		builder.setFenjieHeroCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
