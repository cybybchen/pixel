package com.trans.pixel.view;

import com.trans.pixel.model.userinfo.UserBean;

public class ResultInfo {

    private boolean success;
    
    private UserBean user;

    public ResultInfo(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public ResultInfo(boolean success, String message, UserBean user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public UserBean getUser() {
        return user;
    }
    public void setUser(UserBean user) {
        this.user = user;
    }
    public ResultInfo(){}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
