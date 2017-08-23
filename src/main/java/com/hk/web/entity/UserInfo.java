package com.hk.web.entity;

import com.google.gson.annotations.SerializedName;

public class UserInfo extends Base {
    @SerializedName("ID")
    protected long id;

    @SerializedName("UserName")
    protected String userName;

    @SerializedName("AccountID")
    protected long accountID;

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public long getAccountID() {
        return accountID;
    }
}
