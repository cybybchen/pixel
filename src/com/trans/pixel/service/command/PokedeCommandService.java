package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestChoseClearInfoCommand;
import com.trans.pixel.protoc.Commands.RequestClearHeroCommand;
import com.trans.pixel.protoc.Commands.RequestFeedFoodCommand;
import com.trans.pixel.protoc.Commands.RequestUserPokedeCommand;
import com.trans.pixel.protoc.Commands.ResponseClearInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserPokedeCommand;
import com.trans.pixel.service.ClearService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.FoodService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserClearService;
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
	@Resource
	private UserClearService userClearService;
	@Resource
	private ClearService clearService;
	@Resource
	private RewardService rewardService;
	
	public void getUserPokedeList(RequestUserPokedeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		List<UserPokedeBean> userPokedeList = userPokedeService.selectUserPokedeList(user.getId());
		builder.addAllPokede(buildHeroInfo(userPokedeList, user));
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
		builder.addPokede(userPokede.buildUserPokede(userClearService.selectUserClear(user, heroId)));
		responseBuilder.setUserPokedeCommand(builder.build());
		pushCommandService.pushUserDataByRewardId(responseBuilder, user, foodId);
	}
	
	public void clearPokede(RequestClearHeroCommand cmd, Builder responseBuilder, UserBean user) {
		int position = cmd.getPosition();
		int heroId = cmd.getHeroId();
		int type = cmd.getType();
		int count = 1;
		if (cmd.hasCount())
			count = cmd.getCount();
		
		List<UserClearBean> clearList = new ArrayList<UserClearBean>();
		
		UserPokedeBean userPokede = userPokedeService.selectUserPokede(user, heroId);
		ResultConst result = clearService.clearHero(userPokede, position, type, user, count, clearList);
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
//		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
//		builder.addPokede(userPokede.buildUserPokede(userClearService.selectUserClear(user, heroId)));
//		responseBuilder.setUserPokedeCommand(builder.build());
		ResponseClearInfoCommand.Builder builder = ResponseClearInfoCommand.newBuilder();
		builder.addAllClearInfo(this.buildClearInfo(clearList));
		responseBuilder.setClearInfoCommand(builder.build());
	}
	
	public void choseClearInfo(RequestChoseClearInfoCommand cmd, Builder responseBuilder, UserBean user) {
		int id = 0;
		if (cmd.hasId())
			id = cmd.getId();
		boolean refused = cmd.getRefused();
		if (refused)
			return;
		
		UserClearBean userClear = new UserClearBean();
		
		ResultConst result = clearService.choseClear(user, id, userClear);
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		UserPokedeBean userPokede = userPokedeService.selectUserPokede(user, userClear.getHeroId());
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		builder.addPokede(userPokede.buildUserPokede(userClearService.selectUserClear(user, userClear.getHeroId())));
		responseBuilder.setUserPokedeCommand(builder.build());
	}
}
