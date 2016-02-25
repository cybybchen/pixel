package com.trans.pixel.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Commodity;
import com.trans.pixel.protoc.Commands.ShopList;
import com.trans.pixel.service.redis.ShopRedisService;

/**
 * 后台管理
 */
@Service
public class ManagerService {
	Logger logger = Logger.getLogger(ManagerService.class);
	@Resource
    private ShopRedisService redis;
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	
	public void getData(JSONObject req, HttpServletResponse response) {
		if(req.containsKey("Area")){
			
		}
		
	}
}
