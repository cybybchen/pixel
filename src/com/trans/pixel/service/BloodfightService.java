package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.BloodUserBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.BloodfightRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class BloodfightService {
	Logger logger = Logger.getLogger(BloodfightService.class);
	
	private static final int DAILY_RESET_TIMES = 3;
	
	@Resource
	private UserBattletowerService userBattletowerService;
	@Resource
	private BloodfightRedisService redis;
	@Resource
	private UserService userService;
	@Resource
	private UserTeamService userTeamService;
	
	public void enter(UserBean user) {
		redis.enter(user);
	}
	
	public void handle(int serverId) {
		Set<String> userIdSet = redis.getEnterUsers(serverId);
		List<UserInfo> userList = userService.getCaches(serverId, userIdSet);
		for (UserInfo user : userList) {
			redis.addZhanliToBloodfight(serverId, user);
		}
		
		userIdSet = redis.getBloodfightUserSet(serverId);
		List<String> userIdList = new ArrayList<String>();
		for (String userId : userIdSet) {
			userIdList.add(userId);
		}
		List<BloodUserBean> buserList = new ArrayList<BloodUserBean>();
		for (int i = 0; i < 16; ++i) {
			int random = RandomUtils.nextInt(16 - i);
			String userIdStr = userIdList.get(random);
			long userId = TypeTranslatedUtil.stringToLong(userIdStr);
			UserBean user = userService.getUser(userId);
			BloodUserBean buser = new BloodUserBean();
			buser.setPosition1(i);
			buser.setUserId(userId);
			UserTeamBean userTeam = userTeamService.getUserTeam(userId, user.getCurrentTeamid());
			if (userTeam != null)
				buser.setTeamInfo(userTeam.getTeamRecord());
			
			buserList.add(buser);
			userIdList.remove(random);
			
		}
	}
}
