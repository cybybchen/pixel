package com.trans.pixel.controller.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.model.UserBean;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;


public abstract class RequestScreen implements RequestHandle {
	private RequestHandle nullUserErrorHandle = new NullUserErrorHandle();
	
	private static final Logger logger = LoggerFactory.getLogger(RequestScreen.class);
	
	protected abstract boolean handleRegisterCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleLoginCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleCommand(RequestLevelResultCommand cmd, Builder responseBuilder, UserBean user);
	
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
            if (result) {
                result = handleCommand(cmd, responseBuilder, user);
            }
        }
        
        return result;
	}
}
