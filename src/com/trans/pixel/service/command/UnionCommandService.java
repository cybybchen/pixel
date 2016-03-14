package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestApplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestAttackUnionCommand;
import com.trans.pixel.protoc.Commands.RequestCreateUnionCommand;
import com.trans.pixel.protoc.Commands.RequestDefendUnionCommand;
import com.trans.pixel.protoc.Commands.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.Commands.RequestQuitUnionCommand;
import com.trans.pixel.protoc.Commands.RequestReplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestUnionInfoCommand;
import com.trans.pixel.protoc.Commands.RequestUnionListCommand;
import com.trans.pixel.protoc.Commands.RequestUpgradeUnionCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUnionInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseUnionListCommand;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.service.UnionService;

@Service
public class UnionCommandService extends BaseCommandService {
//	private static final Logger log = LoggerFactory.getLogger(UnionCommandService.class);
	
	@Resource
	private UnionService unionService;
	@Resource
	private PushCommandService pushCommandService;

	public void getUnions(RequestUnionListCommand cmd, Builder responseBuilder, UserBean user) {
		List<Union> unions = unionService.getBaseUnions(user);
		ResponseUnionListCommand.Builder builder = ResponseUnionListCommand.newBuilder();
		builder.addAllUnion(unions);
		responseBuilder.setUnionListCommand(builder.build());
	}
	
	public void getUnion(RequestUnionInfoCommand cmd, Builder responseBuilder, UserBean user) {
		Union union = unionService.getUnion(user);
		if(union == null)
			return;
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(union);
		responseBuilder.setUnionInfoCommand(builder.build());
	}
	
	public void create(RequestCreateUnionCommand cmd, Builder responseBuilder, UserBean user) {
		Union union = unionService.create(cmd.getIcon(), cmd.getName(), user);
		if(union == null)
			return;
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(union);
		responseBuilder.setUnionInfoCommand(builder.build());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.CREATE_UNION_SUCCESS));
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void quit(RequestQuitUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand
				.newBuilder();
		long id = user.getId();
		if (cmd.hasId())
			id = cmd.getId();

		unionService.quit(id, responseBuilder, user);

		Union union = unionService.getUnion(user);
		if (union != null) {
			builder.setUnion(union);
			responseBuilder.setUnionInfoCommand(builder.build());
		}
		responseBuilder.setMessageCommand(super
				.buildMessageCommand(SuccessConst.QUIT_UNION_SUCCESS));
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
//	public void delete(RequestDeleteUnionCommand cmd, Builder responseBuilder, UserBean user) {
//		if(unionService.delete(user)){
//			responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.DELETE_UNION_SUCCESS));
//		}else
//			responseBuilder.setErrorCommand(super.buildErrorCommand(ErrorConst.UNION_ERROR));
//		pushCommandService.pushUserInfoCommand(responseBuilder, user);
//	}
	
	public void apply(RequestApplyUnionCommand cmd, Builder responseBuilder, UserBean user) {
		int unionId = cmd.getUnionId();
		unionService.apply(unionId, user);
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.APPLY_UNION_SUCCESS));
	}
	
	public void reply(RequestReplyUnionCommand cmd, Builder responseBuilder, UserBean user) {
		if(user.getUnionJob() == 0){
			responseBuilder.setErrorCommand(super.buildErrorCommand(ErrorConst.PERMISSION_DENIED));
			return;
		}
		ResultConst result = unionService.reply(cmd.getId(), cmd.getReceive(), user);
		buildMessageOrErrorCommand(responseBuilder, result);
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user));
		responseBuilder.setUnionInfoCommand(builder.build());
	}
	
	public void handleMember(RequestHandleUnionMemberCommand cmd, Builder responseBuilder, UserBean user) {
		if(unionService.handleMember(cmd.getId(), cmd.getJob(), user)){
			ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
			builder.setUnion(unionService.getUnion(user));
			responseBuilder.setUnionInfoCommand(builder.build());
			responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.HANDLE_UNION_MEMBER_SUCCESS));
		}else
			responseBuilder.setErrorCommand(super.buildErrorCommand(ErrorConst.PERMISSION_DENIED));
	}
	
	public void upgrade(RequestUpgradeUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.upgrade(user);
		if(result instanceof ErrorConst)
			responseBuilder.setErrorCommand(super.buildErrorCommand(result));
		else
			responseBuilder.setMessageCommand(super.buildMessageCommand(result));
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user));
		responseBuilder.setUnionInfoCommand(builder.build());
	}

	public void attack(RequestAttackUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.attack(cmd.getUnionId(), cmd.getTeamid(), user);
		if(result instanceof ErrorConst)
			responseBuilder.setErrorCommand(this.buildErrorCommand(result));
		else
			responseBuilder.setMessageCommand(buildMessageCommand(result));
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user));
		responseBuilder.setUnionInfoCommand(builder.build());
	}
	public void defend(RequestDefendUnionCommand cmd, Builder responseBuilder, UserBean user) {
		ResultConst result = unionService.defend(cmd.getTeamid(), user);
		if(result instanceof ErrorConst)
			responseBuilder.setErrorCommand(super.buildErrorCommand(result));
			
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(unionService.getUnion(user));
		responseBuilder.setUnionInfoCommand(builder.build());
	}
}
