package com.trans.pixel.controller.chain;


public interface RequestHandle {
    public boolean handleRequest(PixelRequest request, PixelResponse response);
}
