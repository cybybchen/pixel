package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.model.UserBean;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.AccountService;
import com.trans.pixel.service.UserService;


public class HeadScreen extends RequestScreen {
	
	private static final Logger log = LoggerFactory.getLogger(HeadScreen.class);
	
	@Resource
    private AccountService accountService;
	@Resource
    private UserService userService;
	
	@Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
        RequestCommand request = req.command;
        HeadInfo head = request.getHead();
        HeadInfo.Builder headBuilder = buildHeadInfo(head);
        HeadInfo responseHead = headBuilder.build();
        rep.command.setHead(responseHead);
        if (request.hasRegisterCommand() || request.hasLoginCommand()) {
            return true;
        }
        long userId = head.getUserId();
        req.user = userService.getUser(userId);
        
        if (req.user != null) {
           
        } else {
            log.error("cmd user err : " + req);
        }
        
        return true;
    }

    private HeadInfo.Builder buildHeadInfo(HeadInfo head) {
        HeadInfo.Builder headBuilder = HeadInfo.newBuilder();
        headBuilder.setGameVersion(head.getGameVersion());
        headBuilder.setVersion(head.getVersion());
        headBuilder.setAccount(head.getAccount());
        headBuilder.setServerId(head.getServerId());
        headBuilder.setUserId(head.getUserId());
        headBuilder.setDatetime(System.currentTimeMillis() / 1000);
        return headBuilder;
    }

	@Override
	protected boolean handleRegisterCommand(RequestCommand cmd,
			Builder responseBuilder) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleLoginCommand(RequestCommand cmd,
			Builder responseBuilder) {
		// TODO Auto-generated method stub
		return true;
	}
}
