package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.BloodUserBean;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.service.redis.BloodfightRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class BloodfightService {
	Logger logger = Logger.getLogger(BloodfightService.class);
	
//	private static final int DAILY_RESET_TIMES = 3;
	
	@Resource
	private UserBattletowerService userBattletowerService;
	@Resource
	private BloodfightRedisService redis;
	@Resource
	private UserService userService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private CostService costService;
	@Resource
	private MailService mailService;
	
	Comparator<BloodUserBean> comparator = new Comparator<BloodUserBean>() {
        public int compare(BloodUserBean bean1, BloodUserBean bean2) {
                if (bean1.getCurrentPosition() < bean2.getCurrentPosition()) {
                        return -1;
                } else {
                        return 1;
                }
        }
	};
	
	public void enter(UserBean user) {
		redis.enter(user);
	}
	
	public ResultConst xiazhu(UserBean user, long xiazhuUserId) {
		if (!costService.costAndUpdate(user, RewardConst.JEWEL, 100))
			return ErrorConst.NOT_ENOUGH_JEWEL;
		
		redis.xiazhu(user, xiazhuUserId);
		
		return SuccessConst.XIAZHU_SUCCESS;	
	}
	
	public void updateTeamInfo(UserBean user, String teamInfo) {
		BloodUserBean buser = redis.getBloodUser(user);
		buser.setTeamInfo(teamInfo);
		redis.setBloodUser(user.getServerId(), buser);
	}
	
	public void handle(int serverId) {
		int weekDay = DateUtil.getWeekDay();
		switch (weekDay) {
			case 2://星期2
				fight_16(serverId);
				break;
			case 3:
				fight_8(serverId);
				break;
			case 4:
				fight_4(serverId);
				break;
			case 5:
				fight_2(serverId);
				break;
			case 6:
				fight_1(serverId);
				break;
//			case 7:
//				fight_8(serverId);
//				break;
			default:
				break;
		}
	}
	
	private void fight_16(int serverId) {
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
			buser.setCurrentPosition(i);
			buser.setUserId(userId);
			UserTeamBean userTeam = userTeamService.getUserTeam(userId, user.getCurrentTeamid());
			if (userTeam != null)
				buser.setTeamInfo(userTeam.getTeamRecord());
			
			buserList.add(buser);
			userIdList.remove(random);
			
		}
		
		redis.setBloodUserList(serverId, buserList);
	}
	
	private void fight_8(int serverId) {
		List<Long> winUserIds = new ArrayList<Long>(); 
		List<BloodUserBean> buserList = redis.getBloodUserList(serverId);
		Collections.sort(buserList, comparator);
		for (int i = 0; i < buserList.size(); i+=2) {
			BloodUserBean buser = buserList.get(i);
			BloodUserBean nextBuser = buserList.get(i + 1);
			
			//模拟buser win
			nextBuser.setSurvival(false);
			buser.setPosition2((buser.getCurrentPosition() + 1) / 2);
			buser.setCurrentPosition((buser.getCurrentPosition() + 1) / 2);
			winUserIds.add(buser.getUserId());
		}
		
		redis.setBloodUserList(serverId, buserList);
		
		doReward(serverId, winUserIds, 1);
	}
	
	private void fight_4(int serverId) {
		List<Long> winUserIds = new ArrayList<Long>(); 
		List<BloodUserBean> buserList = redis.getBloodUserList(serverId);
		Collections.sort(buserList, comparator);
		for (int i = 0; i < buserList.size(); i+=2) {
			BloodUserBean buser = buserList.get(i);
			BloodUserBean nextBuser = buserList.get(i + 1);
			
			//模拟buser win
			nextBuser.setSurvival(false);
			buser.setPosition3((buser.getCurrentPosition() + 1) / 2);
			buser.setCurrentPosition((buser.getCurrentPosition() + 1) / 2);
			winUserIds.add(buser.getUserId());
		}
		
		redis.setBloodUserList(serverId, buserList);
		
		doReward(serverId, winUserIds, 2);
	}
	
	private void fight_2(int serverId) {
		List<Long> winUserIds = new ArrayList<Long>(); 
		List<BloodUserBean> buserList = redis.getBloodUserList(serverId);
		Collections.sort(buserList, comparator);
		for (int i = 0; i < buserList.size(); i+=2) {
			BloodUserBean buser = buserList.get(i);
			BloodUserBean nextBuser = buserList.get(i + 1);
			
			//模拟buser win
			nextBuser.setSurvival(false);
			buser.setPosition4((buser.getCurrentPosition() + 1) / 2);
			buser.setCurrentPosition((buser.getCurrentPosition() + 1) / 2);
			winUserIds.add(buser.getUserId());
		}
		
		redis.setBloodUserList(serverId, buserList);
		
		doReward(serverId, winUserIds, 3);
	}
	
	private void fight_1(int serverId) {
		List<Long> winUserIds = new ArrayList<Long>(); 
		List<BloodUserBean> buserList = redis.getBloodUserList(serverId);
		Collections.sort(buserList, comparator);
		for (int i = 0; i < buserList.size(); i+=2) {
			BloodUserBean buser = buserList.get(i);
			BloodUserBean nextBuser = buserList.get(i + 1);
			
			//模拟buser win
			nextBuser.setSurvival(false);
			buser.setPosition5((buser.getCurrentPosition() + 1) / 2);
			buser.setCurrentPosition((buser.getCurrentPosition() + 1) / 2);
			winUserIds.add(buser.getUserId());
		}
		
		redis.setBloodUserList(serverId, buserList);
		
		doReward(serverId, winUserIds, 4);
	}
	
	private void doReward(int serverId, List<Long> winUserIds, int turn) {
		Map<String, String> xiazhuMap = redis.getXiazhuMap(serverId);
		Iterator<Entry<String, String>> it = xiazhuMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			long xiazhuUserId = TypeTranslatedUtil.stringToLong(entry.getValue());
			if (winUserIds.contains(xiazhuUserId)) {
				sendXiazhuReward(TypeTranslatedUtil.stringToLong(entry.getKey()), xiazhuUserId, 1);
			}
		}
	}
	
	private void sendXiazhuReward(long userId, long xiazhuUserId, int turn) {
		String content = "恭喜您在血战第" + turn + "轮中下注成功";
		List<RewardBean> rewardList = RewardBean.initRewardList(RewardConst.JEWEL, 200);
		MailBean mail = MailBean.buildSystemMail(userId, content, RewardBean.buildRewardInfoList(rewardList));
		mailService.addMail(mail);
	}
}
