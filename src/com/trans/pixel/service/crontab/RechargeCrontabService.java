package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.model.mapper.RechargeMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.UserService;

@Service
public class RechargeCrontabService {
	private static final Logger log = LoggerFactory.getLogger(RechargeCrontabService.class);

	@Resource
	private RechargeMapper rechargeMapper;
	@Resource
	private RechargeService rechargeService;
	@Resource
	private UserService userService;
	
	@Scheduled(cron = "0 0 0 18 8 ?")
//	@Transactional(rollbackFor=Exception.class)
	public void updateRechargeRecord() {
		log.debug("11");
		List<RechargeBean> rechargeList = rechargeMapper.getRechargeRecord();
		log.debug("size is:" + rechargeList.size());
		if (rechargeList != null && rechargeList.size() > 0) {
			for (RechargeBean recharge : rechargeList) {
				if (recharge.getProductId() > 6) {
					log.debug("222");
					UserBean user = userService.getUser(recharge.getUserId());
					if (user != null)
						rechargeService.buchangVip(user, recharge.getRmb(), recharge.getRmb() / 10);
				}
			}
		}
	}
}
