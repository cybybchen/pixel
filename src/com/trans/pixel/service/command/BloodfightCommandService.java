package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.UnionProto.RequestBloodEnterCommand;
import com.trans.pixel.protoc.UnionProto.RequestBloodXiazhuCommand;
import com.trans.pixel.service.BloodfightService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.UserBattletowerService;

@Service
public class BloodfightCommandService extends BaseCommandService {

	@Resource
	private PushCommandService push;
	@Resource
	private BloodfightService bloodfightService;
	@Resource
	private LogService logService;
	@Resource
	private UserBattletowerService userBattletowerService;
	
	public void bloodfightEnter(RequestBloodEnterCommand cmd, Builder responseBuilder, UserBean user) {
		bloodfightService.enter(user);
	}
	
	public void bloodfightXiazhu(RequestBloodXiazhuCommand cmd, Builder responseBuilder, UserBean user) {
		bloodfightService.xiazhu(user, cmd.getXiazhuUserId());
	}
}
