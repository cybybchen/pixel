package com.trans.pixel.controller.chain;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.protoc.Commands.ErrorCommand;

public class NullUserErrorHandle implements RequestHandle {
    @Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
        ErrorCommand.Builder errBuilder = ErrorCommand.newBuilder();
        errBuilder.setCode(String.valueOf(ErrorConst.USER_NOT_EXIST.getCode()));
        errBuilder.setMessage(ErrorConst.USER_NOT_EXIST.getMesssage());
        rep.command.setErrorCommand(errBuilder.build());
        return false;
    }
}
