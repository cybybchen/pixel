package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class HeroTest extends BaseTest {
	@Test
	public void test(){
		login();
		testHeroLevelUpTest();
	}
	
	public void testHeroLevelUpTest() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestHeroLevelUpCommand.Builder b = RequestHeroLevelUpCommand.newBuilder();
		b.setHeroId(42);
		b.setInfoId(1);
		b.setLevelUpType(1);
		requestBuilder.setHeroLevelUpCommand(b.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
//	public void testFenjieHeroTest() {
//		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
//		requestBuilder.setHead(head());
//		RequestFenjieHeroCommand.Builder b = RequestFenjieHeroCommand.newBuilder();
//		if(loginResponse.getGetUserHeroCommand().getUserHeroCount() == 0)
//			return;
//		HeroInfo userHero = loginResponse.getGetUserHeroCommand().getUserHero(0);
//		FenjieHeroInfo.Builder fenjieHero = FenjieHeroInfo.newBuilder();
//		fenjieHero.setHeroId(userHero.getHeroId());
//		fenjieHero.setInfoId(userHero.getInfoId());
//		b.addFenjieHero(fenjieHero.build());
//		builder.setFenjieHeroCommand(b.build());
//		
//		RequestCommand reqcmd = builder.build();
//		byte[] reqData = reqcmd.toByteArray();
//       InputStream input = new ByteArrayInputStream(reqData);
//       ResponseCommand response = http.post(url, input);
//       Assert.assertNotNull(response);
//	}
}
