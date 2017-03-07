package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestEquipPokedeCommand;
import com.trans.pixel.protoc.Commands.RequestEquipStrenthenCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseEquipPokedeCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.EquipPokedeService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserClearService;
import com.trans.pixel.service.UserEquipPokedeService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class EquipPokedeCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(EquipPokedeCommandService.class);
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private LogService logService;
	@Resource
	private CostService costService;
	@Resource
	private UserClearService userClearService;
	@Resource
	private EquipPokedeService equipPokedeService;
	@Resource
	private RewardService rewardService;
	@Resource
	private ActivityService activityService;
	
	public void getUserEquipPokedeList(RequestEquipPokedeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		List<UserEquipPokedeBean> userPokedeList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
		builder.addAllUserEquipPokede(buildEquipPokede(userPokedeList));
		responseBuilder.setEquipPokedeCommand(builder.build());
	}
	
	public void heroStrengthen(RequestEquipStrenthenCommand cmd, Builder responseBuilder, UserBean user) {
		int itemId = cmd.getItemId();
		List<UserPropBean> propList = new ArrayList<UserPropBean>();
		UserEquipPokedeBean pokede = userEquipPokedeService.selectUserEquipPokede(user, itemId);
		ResultConst result = equipPokedeService.heroStrengthen(pokede, user, propList);
		pushCommandService.pushUserPropListCommand(responseBuilder, user, propList);
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand(result);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		userEquipPokedeService.updateUserEquipPokede(pokede, user);
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		builder.addUserEquipPokede(pokede.build());
		responseBuilder.setEquipPokedeCommand(builder.build());
	}
}
