package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestEquipComposeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseEquipComposeCommand;
import com.trans.pixel.service.EquipService;

@Service
public class EquipCommandService extends BaseCommandService {

	@Resource
	private EquipService equipService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void equipLevelup(RequestEquipComposeCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseEquipComposeCommand.Builder builder = ResponseEquipComposeCommand.newBuilder();
		int originalId = cmd.getOriginalId();
		int levelUpId = cmd.getLevelUpId();
		
		ResultConst result = equipService.equipCompose(user, originalId, levelUpId);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			builder.setEquipId(levelUpId);
			responseBuilder.setEquipComposeCommand(builder.build());
			pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		}
	}
}
