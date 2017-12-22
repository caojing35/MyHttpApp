package com.mustache.myhttpapp;

import com.google.gson.Gson;

/**
 * Created by caojing on 2017/12/22.
 */

public class UploadReq {

    String id;

    String version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
