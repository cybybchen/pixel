package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.HeroProto.RequestChoseClearInfoCommand;
import com.trans.pixel.protoc.HeroProto.RequestClearHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestFeedFoodCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroStrengthenCommand;
import com.trans.pixel.protoc.HeroProto.RequestOpenFetterCommand;
import com.trans.pixel.protoc.HeroProto.RequestUserPokedeCommand;
import com.trans.pixel.protoc.HeroProto.ResponseClearInfoCommand;
import com.trans.pixel.protoc.HeroProto.ResponseUserPokedeCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.ClearService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.FoodService;
import com.trans.pixel.service.HeroService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserClearService;
import com.trans.pixel.service.UserPokedeService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class PokedeCommandService extends BaseCommandService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PokedeCommandService.class);
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
	private HeroService heroService;
	@Resource
	private ActivityService activityService;
	
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
		List<UserPropBean> userPropList = new ArrayList<UserPropBean>();
		UserPokedeBean userPokede = userPokedeService.selectUserPokede(user, heroId);
		ResultConst result = clearService.clearHero(userPokede, position, type, user, count, clearList, userPropList);
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
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		pushCommandService.pushUserPropListCommand(responseBuilder, user, userPropList);
	}
	
	public void choseClearInfo(RequestChoseClearInfoCommand cmd, Builder responseBuilder, UserBean user) {
		int id = 0;
		if (cmd.hasId())
			id = cmd.getId();
		boolean refused = cmd.getRefused();
		if (refused)
			return;
		
		UserClearBean userClear = clearService.choseClear(user, id);
		
		if (userClear == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.CLEAR_CHOSE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.CLEAR_CHOSE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		UserPokedeBean userPokede = userPokedeService.selectUserPokede(user, userClear.getHeroId());
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		if(userPokede != null)
			builder.addPokede(userPokede.buildUserPokede(userClearService.selectUserClear(user, userClear.getHeroId())));
		responseBuilder.setUserPokedeCommand(builder.build());
	}
	
	public void heroStrengthen(RequestHeroStrengthenCommand cmd, Builder responseBuilder, UserBean user) {
		int heroId = cmd.getHeroId();
		List<UserPropBean> propList = new ArrayList<UserPropBean>();
		UserPokedeBean userPokede = userPokedeService.selectUserPokede(user, heroId);
		ResultConst result = clearService.heroStrengthen(userPokede, user, propList);
		pushCommandService.pushUserPropListCommand(responseBuilder, user, propList);
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand(result);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		userPokedeService.updateUserPokede(userPokede, user);
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		builder.addPokede(userPokede.buildUserPokede(userClearService.selectUserClear(user, heroId)));
		responseBuilder.setUserPokedeCommand(builder.build());
		responseBuilder.setMessageCommand(this.buildMessageCommand(result));
		
		/**
		 * activity
		 */
		if (result.getCode() == SuccessConst.HERO_STRENGTHEN_SUCCESS.getCode())
			activityService.heroStrengthen(user, userPokede.getStrengthen());
	}
	
	public void openFetters(RequestOpenFetterCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		int heroId = cmd.getHeroId();
		int fetterId = cmd.getFetterid();
		UserPokedeBean userPokede = userPokedeService.selectUserPokede(user, heroId);
		ResultConst result = heroService.openFetter(user, userPokede, fetterId);
		if (result instanceof ErrorConst) {
			builder.addPokede(userPokede.buildUserPokede(userClearService.selectUserClear(user, heroId)));
			responseBuilder.setUserPokedeCommand(builder.build());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand(result);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		userPokedeService.updateUserPokede(userPokede, user);
		builder.addPokede(userPokede.buildUserPokede(userClearService.selectUserClear(user, heroId)));
		responseBuilder.setUserPokedeCommand(builder.build());
		
		/**
		 * activity
		 */
		activityService.openFetters(user);
	}
}
