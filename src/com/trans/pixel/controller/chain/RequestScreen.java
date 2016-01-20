package com.trans.pixel.controller.chain;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestAddFriendCommand;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestAttackRelativeCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestDeleteMailCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryHeroCommand;
import com.trans.pixel.protoc.Commands.RequestReadMailCommand;
import com.trans.pixel.protoc.Commands.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshRelatedUserCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;


public abstract class RequestScreen implements RequestHandle {
	private RequestHandle nullUserErrorHandle = new NullUserErrorHandle();
	
	protected abstract boolean handleRegisterCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleLoginCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleCommand(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelResultCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelLootStartCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAttackRelativeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLootResultCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestHeroLevelUpCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAddHeroEquipCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUpdateTeamCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestRefreshRelatedUserCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLotteryHeroCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetLadderRankListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAttackLadderModeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetUserMailListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestReadMailCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestDeleteMailCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAddFriendCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestReceiveFriendCommand cmd, Builder responseBuilder, UserBean user);
	
	@Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
        boolean result = true;
        RequestCommand request = req.command;
        ResponseCommand.Builder responseBuilder = rep.command;
        UserBean user = req.user;
        
        if (request.hasRegisterCommand()) {
        	handleRegisterCommand(request, responseBuilder);
        } else if (request.hasLoginCommand()) {
        	handleLoginCommand(request, responseBuilder);
        } else {
        	if (user == null) {
                nullUserErrorHandle.handleRequest(req, rep);
                return false;
            }
        }
        
        if (request.hasLevelResultCommand()) {
        	RequestLevelResultCommand cmd = request.getLevelResultCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAddHeroEquipCommand()) {
        	RequestAddHeroEquipCommand cmd = request.getAddHeroEquipCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAttackRelativeCommand()) {
        	RequestAttackRelativeCommand cmd = request.getAttackRelativeCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasHeroLevelUpCommand()) {
        	RequestHeroLevelUpCommand cmd = request.getHeroLevelUpCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLevelLootResultCommand()) {
        	RequestLevelLootResultCommand cmd = request.getLevelLootResultCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLevelLootStartCommand()) {
        	RequestLevelLootStartCommand cmd = request.getLevelLootStartCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLevelStartCommand()) {
        	RequestLevelStartCommand cmd = request.getLevelStartCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLootResultCommand()) {
        	RequestLootResultCommand cmd = request.getLootResultCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasRefreshRelatedUserCommand()) {
        	RequestRefreshRelatedUserCommand cmd = request.getRefreshRelatedUserCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUpdateTeamCommand()) {
        	RequestUpdateTeamCommand cmd = request.getUpdateTeamCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLotteryHeroCommand()) {
        	RequestLotteryHeroCommand cmd = request.getLotteryHeroCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasGetLadderRankListCommand()) {
        	RequestGetLadderRankListCommand cmd = request.getGetLadderRankListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAttackLadderModeCommand()) {
        	RequestAttackLadderModeCommand cmd = request.getAttackLadderModeCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasGetUserMailListCommand()) {
        	RequestGetUserMailListCommand cmd = request.getGetUserMailListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasReadMailCommand()) {
        	RequestReadMailCommand cmd = request.getReadMailCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasDeleteMailCommand()) {
        	RequestDeleteMailCommand cmd = request.getDeleteMailCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAddFriendCommand()) {
        	RequestAddFriendCommand cmd = request.getAddFriendCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasReceiveFriendCommand()) {
        	RequestReceiveFriendCommand cmd = request.getReceiveFriendCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        
        return result;
	}
}
