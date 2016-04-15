package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;

import com.trans.pixel.protoc.Commands.FenjieHeroInfo;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.test.BaseTest;

public class HeroTest extends BaseTest {
	
	public void testHeroLevelUpTest(RequestCommand req, ResponseCommand loginResponse, int type) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestHeroLevelUpCommand.Builder b = RequestHeroLevelUpCommand.newBuilder();
		if(loginResponse.getGetUserHeroCommand().getUserHeroCount() == 0)
			return;
		HeroInfo userHero = loginResponse.getGetUserHeroCommand().getUserHero(0);
		b.setHeroId(userHero.getHeroId());
		b.setInfoId(userHero.getInfoId());
		b.setLevelUpType(type);
		builder.setHeroLevelUpCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
	public void testFenjieHeroTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestFenjieHeroCommand.Builder b = RequestFenjieHeroCommand.newBuilder();
		if(loginResponse.getGetUserHeroCommand().getUserHeroCount() == 0)
			return;
		HeroInfo userHero = loginResponse.getGetUserHeroCommand().getUserHero(0);
		FenjieHeroInfo.Builder fenjieHero = FenjieHeroInfo.newBuilder();
		fenjieHero.setHeroId(userHero.getHeroId());
		fenjieHero.setInfoId(userHero.getInfoId());
		b.addFenjieHero(fenjieHero.build());
		builder.setFenjieHeroCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
