package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.mapper.TeamRaidMapper;
import com.trans.pixel.model.userinfo.TeamRaidBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.RewardTaskProto.EventProgress;
import com.trans.pixel.protoc.RewardTaskProto.ResponseTeamRaidCommand;
import com.trans.pixel.protoc.RewardTaskProto.RoomInfo;
import com.trans.pixel.protoc.RewardTaskProto.TeamRaid;
import com.trans.pixel.protoc.RewardTaskProto.TeamRaidList;
import com.trans.pixel.protoc.RewardTaskProto.UserRoom;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class TeamRaidRedisService extends RedisService{
	@Resource
	private MailService mailService;
	@Resource
	private TeamRaidMapper mapper;
	
	Logger logger = Logger.getLogger(TeamRaidRedisService.class);
	final public int INDEX_SIZE = 100000; 
	
	public TeamRaidRedisService() {
		buildTeamRaidListConfig();
	}
	
	public void unlock(UserBean user, int unlock) {
		ResponseTeamRaidCommand.Builder raidlist = getTeamRaid(user);
		for(TeamRaid.Builder myraid : raidlist.getRaidBuilderList()) {
			if(myraid.getStatus() == 0 && myraid.getUnlock() == unlock) {
				myraid.setStatus(1);
				saveTeamRaid(user, myraid);
				sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERTEAMRAID_PREFIX, user.getId()+"#"+myraid.getId());
			}
		}
	}

	public String popDBKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERTEAMRAID_PREFIX);
	}
	public void updateToDB(long userId, int id){
		String value = hget(RedisKey.USERTEAMRAID_PREFIX+userId, id+"", userId);
		TeamRaidBean bean = TeamRaidBean.fromJson(userId, value);
		if(bean != null)
			mapper.updateRaid(bean);
	}

	public TeamRaid.Builder getTeamRaid(long userId, int id){
//		int id = index/INDEX_SIZE;
		String value = hget(RedisKey.USERTEAMRAID_PREFIX+userId, id+"", userId);
		TeamRaid.Builder myraid = TeamRaid.newBuilder();
		if(value != null && parseJson(value, myraid)) {
			return myraid;
		}
		return null;
	}
	
	private int getRaidStatus(List<TeamRaidBean> list, int id) {
		for(TeamRaidBean bean : list) {
			if(bean.getId() == id)
				return bean.getStatus();
		}
		return 0;
	}
	
	public ResponseTeamRaidCommand.Builder getTeamRaid(UserBean user){
		ResponseTeamRaidCommand.Builder builder = getTeamRaidList();
		Map<String, String> keyvalue = hget(RedisKey.USERTEAMRAID_PREFIX+user.getId(), user.getId());
		List<TeamRaidBean> list = new ArrayList<TeamRaidBean>();
		if(keyvalue.isEmpty()) {
			list = mapper.getRaids(user.getId());
		}
		long endtime = nextDay(0);
		for(int i = builder.getRaidCount()-1; i >= 0; i--) {
			TeamRaid.Builder raid = builder.getRaidBuilder(i);
			String value = keyvalue.get(raid.getId()+"");
			TeamRaid.Builder myraid = TeamRaid.newBuilder();
			if(value != null && parseJson(value, myraid)) {
				raid.clearCost();
				raid.setLeftcount(myraid.getLeftcount());
				raid.setEndtime(myraid.getEndtime());
				if(myraid.hasRoomInfo())
					raid.setRoomInfo(myraid.getRoomInfo());
				raid.setIndex(myraid.getIndex());
				raid.setStatus(myraid.getStatus());
				if(myraid.getEventCount() == 0)
					raid.clearEvent();
				for(int j = 0; j < raid.getEventCount() && j < myraid.getEventCount(); j++) {
					raid.getEventBuilder(j).setStatus(myraid.getEventBuilder(j).getStatus());
				}
			}else {
				raid.clearEvent();
				raid.setStatus(getRaidStatus(list, raid.getId()));
			}
			if(raid.getEndtime() < endtime) {//刷新次数
				raid.setLeftcount(raid.getCount());
				raid.setEndtime(endtime);
				if(raid.getIndex()/INDEX_SIZE != raid.getId()) {
					raid.setIndex((raid.getIndex()+1)%INDEX_SIZE+raid.getId()*INDEX_SIZE);
				}
				saveTeamRaid(user, raid);
			}
		}
		return builder;
	}

	public void saveTeamRaid(UserBean user, TeamRaid.Builder raid){
		saveTeamRaid(user.getId(), raid);
	}
	
	public void saveTeamRaid(long userId, TeamRaid.Builder raid){
		hput(RedisKey.USERTEAMRAID_PREFIX+userId, raid.getId()+"", formatJson(raid.build()), userId);
		expire(RedisKey.USERTEAMRAID_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
//		if(updateDB)
//			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERTEAMRAID_PREFIX, user.getId()+"#"+raid.getId());
	}

//	public String popDBKey() {
//		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERRAID_PREFIX);
//	}
//	public void updateToDB(long userId, int id){
//		String value = hget(RedisKey.USERRAID_PREFIX+userId, id+"", userId);
//		TeamRaidBean bean = TeamRaidBean.fromJson(userId, value);
//		if(bean != null)
//			mapper.updateTeamRaid(bean);
//	}
	
//	public void deleteTeamRaid(UserBean user, int id){
//		hdelete(RedisKey.USERRAID_PREFIX+user.getId(), id+"");
//	}

//	public EventConfig getTeamRaidLevel(int level) {
//		Map<Integer, EventConfig> map = CacheService.hgetcache(RedisKey.RAIDLEVEL_CONFIG);
//		return map.get(level);
//	}
//
//	private void buildTeamRaidLevelConfig() {
//		EventConfigList.Builder list = EventConfigList.newBuilder();
//		Map<Integer, EventConfig> map = new HashMap<Integer, EventConfig>();
//		String xml = ReadConfig("ld_raidlevel.xml");
//		parseXml(xml, list);
//		for(EventConfig config : list.getDataList()) {
//			map.put(config.getId(), config);
//		}
//		CacheService.hputcacheAll(RedisKey.RAIDLEVEL_CONFIG, map);
//	}
//	
	public TeamRaid getTeamRaid(int id){
		ResponseTeamRaidCommand raidList = CacheService.getcache(RedisKey.TEAMRAID_CONFIG);
		for (TeamRaid raid : raidList.getRaidList()) {
			if (raid.getId() == id)
				return raid;
		}
		return null;
	}

	public ResponseTeamRaidCommand.Builder getTeamRaidList(){
		ResponseTeamRaidCommand raids = CacheService.getcache(RedisKey.TEAMRAID_CONFIG);
		ResponseTeamRaidCommand.Builder builder = ResponseTeamRaidCommand.newBuilder(raids);
		return builder;
	}

	private void buildTeamRaidListConfig(){
		String xml = ReadConfig("ld_teamraid.xml");
		TeamRaidList.Builder list = TeamRaidList.newBuilder();
		parseXml(xml, list);
		for(TeamRaid.Builder raid : list.getDataBuilderList()) {
			for(EventProgress.Builder progress : raid.getEventBuilderList()) {
				progress.setStatus(0);
			}
		}
		ResponseTeamRaidCommand.Builder builder = ResponseTeamRaidCommand.newBuilder();
		builder.addAllRaid(list.getDataList());
		CacheService.setcache(RedisKey.TEAMRAID_CONFIG, builder.build());
	}
	/////////////////////////
	
	public void sendInviteMail(UserBean user, long userId, int id, String content) {
		MailBean mail = MailBean.buildMail(userId, user, content, MailConst.TYPE_INVITE_TEAMRAID_MAIL, id);
		mailService.addMail(mail);
		logger.debug("mail is:" + mail.toJson());
	}
	
	public boolean hasPosition(int position, List<RoomInfo> roomList) {
		for (RoomInfo room : roomList) {
			if (position == room.getPosition())
				return true;
		}
		
		return false;
	}
	
	public UserRoom.Builder getUserRoom(long userId, int index) {
		String key = RedisKey.TEAMRAID_ROOM_PREFIX + userId;
		String value = hget(key, "" + index, userId);
		if (value == null)
			return null;
		
		UserRoom.Builder builder = UserRoom.newBuilder();
		if (parseJson(value, builder)){
				return builder;
		}
		
		return null;
	}
	
	public void saveUserRoom(UserRoom room) {
		String key = RedisKey.TEAMRAID_ROOM_PREFIX + room.getCreateUserId();
		hput(key, "" + room.getIndex(), formatJson(room), room.getCreateUserId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, room.getCreateUserId());
	}
	
	public void delUserRoom(UserBean user, int index) {
		String key = RedisKey.TEAMRAID_ROOM_PREFIX + user.getId();
		hdelete(key, "" + index, user.getId());
	}
}
