package com.trans.pixel.controller.chain;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Request.RequestCommand;

public class PixelRequest {
	public RequestCommand command;
    public UserBean user;
    public String toString(){
    	return ""+command;
    }
}
