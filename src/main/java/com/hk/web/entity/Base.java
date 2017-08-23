package com.hk.web.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Base {
    @SerializedName("Data")
    protected Map<String, String> data;

    public Map<String, String> getData() {
        return data;
    }
}
