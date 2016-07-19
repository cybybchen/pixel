package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestFeedFoodCommand;
import com.trans.pixel.protoc.Commands.RequestUserPokedeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserPokedeCommand;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.FoodService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserPokedeService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class PokedeCommandService extends BaseCommandService {
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private FoodService foodService;
	@Resource
	private LogService logService;
	@Resource
	private CostService costService;
	
	public void getUserPokedeList(RequestUserPokedeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		List<UserPokedeBean> userPokedeList = userPokedeService.selectUserPokedeList(user.getId());
		builder.addAllPokede(buildHeroInfo(userPokedeList));
		responseBuilder.setUserPokedeCommand(builder.build());
	}
	
	public void feedFood(RequestFeedFoodCommand cmd, Builder responseBuilder, UserBean user) {
		int heroId = cmd.getHeroId();
		int foodId = cmd.getFoodId();
		int foodCount = cmd.getFoodCount();
		
		UserPokedeBean userPokede = userPokedeService.selectUserPokede(user, heroId);
		
		ResultConst result = foodService.feedFood(userPokede, user, foodId, foodCount);
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		if (!costService.cost(user, foodId, foodCount)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.FOOD_NOT_ENOUGH);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.FOOD_NOT_ENOUGH);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		userPokedeService.updateUserPokede(userPokede, user);
		
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		builder.addPokede(userPokede.buildUserPokede());
		responseBuilder.setUserPokedeCommand(builder.build());
	}
}
