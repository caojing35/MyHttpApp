package com.mustache.myhttpapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by caojing on 2017/12/22.
 */

public class TMSServer {

    public static ITMSApi api;

    public static void init()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.3.45:8080/tms/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ITMSApi.class);
    }
}
