package com.trans.pixel.utils;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.protoc.Commands.ResponseCommand;

public class HTTPProtobufResolver implements HTTPBodyResolver<ResponseCommand> {
    private static final Logger logger = LoggerFactory.getLogger(HTTPProtobufResolver.class);

    @Override
    public ResponseCommand solve(InputStream in) {
        try {
            ResponseCommand res = ResponseCommand.parseFrom(in);
            logger.debug("Commands parse result: {}", res.toString());
            return res;
        } catch (IOException e) {
            logger.error("IOException while parsing", e);
        }
        return null;
    }
}
