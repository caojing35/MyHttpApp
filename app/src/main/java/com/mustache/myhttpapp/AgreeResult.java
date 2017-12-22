package com.mustache.myhttpapp;

import java.util.Arrays;

/**
 * Created by caojing on 2017/12/22.
 */

public class AgreeResult {

    String result;

    String[] agree;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String[] getAgree() {
        return agree;
    }

    public void setAgree(String[] agree) {
        this.agree = agree;
    }

    @Override
    public String toString() {
        return "AgreeResult{" +
                "result='" + result + '\'' +
                ", agree=" + Arrays.toString(agree) +
                '}';
    }
}
