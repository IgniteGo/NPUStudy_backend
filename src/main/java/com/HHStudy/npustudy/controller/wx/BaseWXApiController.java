package com.HHStudy.npustudy.controller.wx;

import com.HHStudy.npustudy.context.WxContext;
import com.HHStudy.npustudy.domain.User;
import com.HHStudy.npustudy.domain.UserToken;
import com.HHStudy.npustudy.utility.ModelMapperSingle;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseWXApiController {
    protected final static ModelMapper modelMapper = ModelMapperSingle.Instance();
    @Autowired
    private WxContext wxContext;

    protected User getCurrentUser() {
        return wxContext.getCurrentUser();
    }

    protected UserToken getUserToken() {
        return wxContext.getCurrentUserToken();
    }
}
