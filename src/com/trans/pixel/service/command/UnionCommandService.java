package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestApplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestCreateUnionCommand;
import com.trans.pixel.protoc.Commands.RequestHandleUnionApplyCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUnionInfoCommand;
import com.trans.pixel.service.UnionService;

@Service
public class UnionCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(UnionCommandService.class);
	
	@Resource
	private UnionService unionService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void applyUnion(RequestApplyUnionCommand cmd, Builder responseBuilder, UserBean user) {
		int unionId = cmd.getUnionId();
		unionService.applyToUnion(user, unionId);
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.APPLY_UNION_SUCCESS));
	}
	
	public void createUnion(RequestCreateUnionCommand cmd, Builder responseBuilder, UserBean user) {
		String unionName = cmd.getUnionName();
		UnionBean union = unionService.createUnion(user, unionName);
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(union.buildUnion());
		responseBuilder.setUnionInfoCommand(builder.build());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.CREATE_UNION_SUCCESS));
	}
	
	public void handleUnionApply(RequestHandleUnionApplyCommand cmd, Builder responseBuilder, UserBean user) {
		int unionId = cmd.getUnionId();
		int mailId = cmd.getMailId();
		boolean receive = cmd.getReceive();
		UnionBean union = unionService.handleUnionApply(user, unionId, mailId, receive);
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(union.buildUnion());
		responseBuilder.setUnionInfoCommand(builder.build());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.HANDLE_UNION_APPLY_SUCCESS));
	}
}
