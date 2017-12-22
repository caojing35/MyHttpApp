package com.mustache.myhttpapp;

/**
 * Created by caojing on 2017/12/22.
 */

public class UploadResult {
    String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "UploadResult{" +
                "result='" + result + '\'' +
                '}';
    }
}
