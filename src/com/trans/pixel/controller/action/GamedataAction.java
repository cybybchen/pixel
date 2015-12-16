package com.trans.pixel.controller.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.trans.pixel.controller.chain.GenericErrorHandle;
import com.trans.pixel.controller.chain.PixelRequest;
import com.trans.pixel.controller.chain.PixelResponse;
import com.trans.pixel.controller.chain.RequestHandle;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;


@Controller
@Scope("prototype")
public class GamedataAction {
	private static final Logger logger = LoggerFactory.getLogger(GamedataAction.class);
	private static final String CONTENT_TYPE = "application/octet-stream";
	
	private static long lastTime = System.currentTimeMillis();
	private static long actions = 0;
	private static long actionCost = 0;
	@Resource
    @Qualifier("chainOfScreens")
    private ArrayList<RequestHandle> chainOfScreens;
	
	private RequestHandle genericErrorHandle = new GenericErrorHandle();

    private PixelRequest httpcmd(HttpServletRequest request) throws IOException {
        InputStream in = request.getInputStream();
        try {
            RequestCommand requestCommand = RequestCommand.parseFrom(in);
            logger.debug("PROCESSING request={}, bytes={}", requestCommand, requestCommand.getSerializedSize());
            PixelRequest ret = new PixelRequest();
            ret.command = requestCommand;
            ret.user = null;
            return ret;
        } finally {
            in.close();
        }
    }

    private void cmdhttp(PixelResponse nfsResponse, HttpServletResponse response) throws IOException {
        ResponseCommand responseCommand = nfsResponse.command.build();
        logger.debug("PROCESSING response={}", responseCommand);
        OutputStream out = response.getOutputStream();
        try {
            responseCommand.writeTo(out);
            out.flush();
        } finally {
            out.close();
        }
    }

    @RequestMapping("/gamedata")
    @ResponseBody
    public void exec(HttpServletRequest request, HttpServletResponse response) {
    	long startTime = System.currentTimeMillis();
		long actionStartTime = System.currentTimeMillis();
        try {
            response.setContentType(CONTENT_TYPE);
            PixelRequest req = null;
            PixelResponse rep = new PixelResponse();
            try {
                req = httpcmd(request);
            } catch (Exception e) {
                logger.error("PIXEL_REQUEST_ERROR", e);
            }
            try {
                boolean result = true;
                for (RequestHandle handle : chainOfScreens) {
                    result = handle.handleRequest(req, rep);
                    if (!result) {
                        break;
                    }
                }
                cmdhttp(rep, response);
            } catch (Exception e) {
                genericErrorHandle.handleRequest(req, rep);
                logger.error("PIXEL_RESPONSE_ERROR", e);
            } finally {
                
            }
        } catch (Throwable e) {
            logger.error("PIXEL_ERRO", e);
        }
        
        logger.debug("ybchen pixel test" + (System.currentTimeMillis() - startTime));
		
		actionCost += (System.currentTimeMillis()-actionStartTime);
		actions++;
		long now = System.currentTimeMillis();
		if(now - lastTime > 5000)
		{
			logger.error("ybchen pixel Time: " + (now - lastTime) + " actions: " + actions + "average cost: " + actionCost / actions);
			actionCost = 0;
			actions = 0;
			lastTime = now;
		}
    }

}
