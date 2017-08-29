package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.mapper.UserFightInfoMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserFightInfoBean;
import com.trans.pixel.protoc.Base.FIGHTINFO_TYPE;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.service.redis.FightInfoRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class FightInfoService {
	private static Logger log = Logger.getLogger(FightInfoService.class);

	@Resource
	private FightInfoRedisService redis;
	@Resource
	private UserService userService;
	@Resource
	private UserFightInfoMapper mapper;
	@Resource
	private RankRedisService rankRedisService;
	
	public void setFightInfo(String info, UserBean user){
		redis.setFightInfo(info, user);
	}
	
	public List<FightInfo.Builder> getFightInfoList(UserBean user){
		List<FightInfo.Builder> infos = redis.getFightInfoList(user);
		for(FightInfo.Builder info : infos) {
			if(info.hasEnemy())
				info.setEnemy(userService.getCache(user.getServerId(), info.getEnemy().getId()));
		}
		return infos;
	}
	
	private FightInfo getFightInfoSelf(UserBean user, int id) {
		for (FightInfo.Builder info : getFightInfoList(user)) {
			if (info.getId() == id)
				return info.build();
		}
		return null;
	}
	
	public ResultConst save(UserBean user, FightInfo fightinfo, int id, FIGHTINFO_TYPE type) {
		if (redis.hlenFightInfo(user) >= 10) {
			return ErrorConst.FIGHTINFO_IS_LIMIT_ERROR;
		}
		if (redis.isExistFightInfoKey(user, id))
			return ErrorConst.FIGHTINFO_IS_EXIST_ERROR;
//		FightInfo.Builder builder = FightInfo.newBuilder(fightinfo);
//		builder.setUser(user.buildShort());
//		if (!builder.hasId() || builder.getId() <= 0)
//			builder.setId((int) ((System.currentTimeMillis() + 12345) % 10000000));
		
		if (type.equals(FIGHTINFO_TYPE.TYPE_RANK)) {
			fightinfo = rankRedisService.getFightInfo(id);
		} else if (type.equals(FIGHTINFO_TYPE.TYPE_SELF)) {
			fightinfo = getFightInfoSelf(user, id);
		}
		
		if (fightinfo == null)
			return ErrorConst.FIGHTINFO_IS_NOT_EXIST_ERROR;
		
		redis.saveFightInfo(user, fightinfo);
		mapper.saveFightInfo(new UserFightInfoBean(user.getId(), fightinfo));
		
		return SuccessConst.SAVE_SUCCESS;
	}
	
	public List<Integer> getSaveFightInfoIds(UserBean user) {
		List<Integer> idList = new ArrayList<Integer>();
		Set<String> ids = redis.saveFightInfoKeys(user);
		if (ids == null || ids.isEmpty()) {
			idList = new ArrayList<Integer>();
			List<UserFightInfoBean> list = mapper.getFightInfos(user.getId());
			if (list != null && !list.isEmpty()) {
				for (UserFightInfoBean bean : list) {
					FightInfo fight = bean.build();
					redis.saveFightInfo(user, fight);
					idList.add(fight.getId());
				}
			}
		} else {
			for (String id : ids) {
				idList.add(TypeTranslatedUtil.stringToInt(id));
			}
		}
		
		return idList;
	}
	
	public void delete(UserBean user, int fightInfoId) {
		redis.deleteFightInfo(user, fightInfoId);
		mapper.removeFightInfo(user.getId(), fightInfoId);
	}
	
	public List<FightInfo> getSaveFightInfoList(UserBean user){
		List<FightInfo> infos = redis.getSaveFightInfoList(user);
		if (infos.isEmpty()) {
			List<UserFightInfoBean> list = mapper.getFightInfos(user.getId());
			if (list != null && !list.isEmpty()) {
				for (UserFightInfoBean bean : list) {
					FightInfo fight = bean.build();
					redis.saveFightInfo(user, fight);
					infos.add(fight);
				}
			}
		}
		
		List<FightInfo> infoList = new ArrayList<FightInfo>();
		for (FightInfo fight : infos) {
			FightInfo.Builder builder = FightInfo.newBuilder(fight);
			if(builder.hasEnemy())
				builder.setEnemy(userService.getCache(user.getServerId(), builder.getEnemy().getId()));
			
			infoList.add(builder.build());
		}
		
		return infoList;
	}
	
	public FightInfo queryFightInfo(UserBean user, FIGHTINFO_TYPE type, int id) {
		if (type.equals(FIGHTINFO_TYPE.TYPE_RANK)) {
			return rankRedisService.getFightInfo(id);
		} else if (type.equals(FIGHTINFO_TYPE.TYPE_SELF)) {
			return getFightInfoSelf(user, id);
		} else if (type.equals(FIGHTINFO_TYPE.TYPE_SAVE)) {
			return redis.getSaveFightInfo(user, id);
		}
		
		return null;
	}
}
