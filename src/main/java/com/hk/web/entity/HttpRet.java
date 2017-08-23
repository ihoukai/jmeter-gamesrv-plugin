package com.hk.web.entity;

import com.google.gson.annotations.SerializedName;

public class HttpRet<T> {
    @SerializedName("code")
    protected long code;

    @SerializedName("error")
    protected String error;

    @SerializedName("data")
    protected T data;

    public long getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public T getData() {
        return data;
    }
}
