package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetUserHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LootService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserHeroService;

@Service
public class PushCommandService extends BaseCommandService {

	@Resource
	private LootService lootService;
	@Resource
	private PvpMapService pvpMapService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private LadderService ladderService;
	
	public void pushLootResultCommand(Builder responseBuilder, UserBean user) {
		user = lootService.updateLootResult(user);
		responseBuilder.setLootResultCommand(super.builderLootResultCommand(user));
	}
	
	public void pushUserMineListCommand(Builder responseBuilder, UserBean user) {
		List<UserMineBean> userMineList = pvpMapService.relateUser(user);
		responseBuilder.setGetUserMineCommand(super.buildGetUserMineCommand(userMineList));
	}
	
	public void pushUserHeroListCommand(Builder responseBuilder, UserBean user) {
		List<UserHeroBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
		ResponseGetUserHeroCommand.Builder builder = ResponseGetUserHeroCommand.newBuilder();
		builder.addAllUserHero(super.buildUserHeroList(userHeroList));
		responseBuilder.setGetUserHeroCommand(builder.build());
	}
	
	public void pushGetUserLadderRankListCommand(Builder responseBuilder, UserBean user) {
		super.handleGetUserLadderRankListCommand(responseBuilder, user);
	}
}
