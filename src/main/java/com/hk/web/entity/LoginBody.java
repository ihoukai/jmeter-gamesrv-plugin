package com.hk.web.entity;

import com.google.gson.annotations.SerializedName;

public class LoginBody {
    @SerializedName("Username")
    public String username;
    @SerializedName("Password")
    public String password;

    public LoginBody(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
