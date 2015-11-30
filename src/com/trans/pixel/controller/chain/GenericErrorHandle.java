package com.trans.pixel.controller.chain;

import com.trans.pixel.protoc.Commands.ErrorCommand;

public class GenericErrorHandle implements RequestHandle {
    @Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
        String code="Response Error";
        String message="Response Error";
        if(rep.command.hasErrorCommand()){
            code =rep.command.getErrorCommand().getCode();
            message=rep.command.getErrorCommand().getMessage();
        }
        ErrorCommand.Builder ecBuilder = ErrorCommand.newBuilder();
        ecBuilder.setCode(code);
        ecBuilder.setMessage(message);
        rep.command.setErrorCommand(ecBuilder.build());
        return false;
    }
}
