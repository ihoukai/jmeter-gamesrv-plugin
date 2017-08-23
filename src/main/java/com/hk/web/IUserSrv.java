package com.hk.web;

import com.hk.web.entity.HttpRet;
import com.hk.web.entity.LoginBody;
import com.hk.web.entity.UserInfo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IUserSrv {
    @POST("auth/login")
    Call<HttpRet<UserInfo>> login(@Body LoginBody body);
}