package com.trans.pixel.test;

import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.EquipProto.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.PVPProto.PVPMap;
import com.trans.pixel.protoc.PVPProto.PVPMine;
import com.trans.pixel.protoc.PVPProto.RequestAttackPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestAttackPVPMonsterCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPMapListCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestUnlockPVPMapCommand;
import com.trans.pixel.protoc.PVPProto.ResponsePVPMapListCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class PvpMapTest extends BaseTest {

	@Test
	public void testPvp() {
		login();
		testPvp(getRequestCommand());
	}
	
	public void testPvp(RequestCommand request) {
//		submitZhanliTest(request);
//		unlockMap(request);
		testPvpMap(request);
		attackMonster(request);
		testGetMineInfo(request);
		attackMine(request);
		testRefreshMine(request);
		testRefreshPvpMap(request);
	}

	private void submitZhanliTest(RequestCommand request){
		RequestSubmitZhanliCommand.Builder builder = RequestSubmitZhanliCommand.newBuilder();
		builder.setZhanli(50000);
//		builder.setZhanli(20000+(int)(System.currentTimeMillis()%30000));

		ResponseCommand response = request("submitZhanliCommand", builder.build(), request);
	}
	
	private void testRefreshPvpMap(RequestCommand request) {
		if(maplist.getEndTime() > System.currentTimeMillis()/1000)
			return;
		RequestRefreshPVPMapCommand.Builder builder = RequestRefreshPVPMapCommand.newBuilder();
        ResponseCommand response = request("refreshPvpMapCommand", builder.build(), request);
        
        if(response != null && response.hasPvpMapListCommand())
        	maplist = response.getPvpMapListCommand();
	}
	
	private ResponsePVPMapListCommand maplist = null;
	private void testPvpMap(RequestCommand request) {
		RequestPVPMapListCommand.Builder builder = RequestPVPMapListCommand.newBuilder();
        ResponseCommand response = request("pvpMapListCommand", builder.build(), request);
        
        if(response != null && response.hasPvpMapListCommand())
        	maplist = response.getPvpMapListCommand();
	}
	
	private void unlockMap(RequestCommand request) {
		RequestUnlockPVPMapCommand.Builder builder = RequestUnlockPVPMapCommand.newBuilder();
		builder.setFieldid(101);
		builder.setZhanli(500000);
        ResponseCommand response = request("unlockPvpMapCommand", builder.build(), request);
        
        if(response != null && response.hasPvpMapListCommand())
        	maplist = response.getPvpMapListCommand();
	}
	
	private void attackMonster(RequestCommand request) {
		int monsterid = 0;
		for(PVPMap map : maplist.getFieldList()){
			if (map.getEventCount() > 0) {
				monsterid = map.getEvent(0).getPositionid();
				break;
			}
		}
		if(monsterid == 0)
			return;
		RequestAttackPVPMonsterCommand.Builder builder = RequestAttackPVPMonsterCommand.newBuilder();
		builder.setPositionid(monsterid);
		builder.setRet(true);
        ResponseCommand response = request("attackPVPMonsterCommand", builder.build(), request);
        
        if(response != null && response.hasPvpMapListCommand())
        	maplist = response.getPvpMapListCommand();
	}
	
	private int getMineId(){
		for(PVPMap map : maplist.getFieldList()){
			for(PVPMine mine : map.getKuangdianList()) {
				if(mine.hasOwner()){
					return mine.getId();
				}
			}
		}
		return 0;
	}
	private void testGetMineInfo(RequestCommand request) {
		int mineid = getMineId();
		if(mineid == 0)
			return;
		RequestPVPMineInfoCommand.Builder builder = RequestPVPMineInfoCommand.newBuilder();
		builder.setId(mineid);
        ResponseCommand response = request("pvpMineInfoCommand", builder.build(), request);
        
        if(response != null && response.hasPvpMapListCommand())
        	maplist = response.getPvpMapListCommand();
	}
	
	private void attackMine(RequestCommand request) {
		int mineid = getMineId();
		if(mineid == 0)
			return;
		RequestAttackPVPMineCommand.Builder builder = RequestAttackPVPMineCommand.newBuilder();
		builder.setId(mineid);
		builder.setTeamid(1);
		builder.setRet(false);
//		builder.setRet(true);
        ResponseCommand response = request("attackPVPMineCommand", builder.build(), request);
        
        if(response != null && response.hasPvpMapListCommand())
        	maplist = response.getPvpMapListCommand();
	}
	
	private void testRefreshMine(RequestCommand request) {
		int mineid = getMineId();
		if(mineid == 0)
			return;
		RequestRefreshPVPMineCommand.Builder builder = RequestRefreshPVPMineCommand.newBuilder();
		builder.setId(mineid);
        ResponseCommand response = request("refreshPVPMineCommand", builder.build(), request);
        
        if(response != null && response.hasPvpMapListCommand())
        	maplist = response.getPvpMapListCommand();
	}
}
