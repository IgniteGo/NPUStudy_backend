package com.HHStudy.npustudy.viewmodel.admin.user;

import com.HHStudy.npustudy.base.BasePage;


/**
 * @author HHStudyGroup
 */


public class UserPageRequestVM extends BasePage {

    private String userName;
    private Integer role;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}