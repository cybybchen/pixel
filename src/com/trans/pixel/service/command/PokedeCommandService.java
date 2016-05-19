package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Commands.RequestUserPokedeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserPokedeCommand;
import com.trans.pixel.service.UserPokedeService;

@Service
public class PokedeCommandService extends BaseCommandService {
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void getUserPokedeList(RequestUserPokedeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		List<UserPokedeBean> userPokedeList = userPokedeService.selectUserPokedeList(user.getId());
		builder.addAllPokede(buildHeroInfo(userPokedeList));
		responseBuilder.setUserPokedeCommand(builder.build());
	}
}
