package com.mustache.myhttpapp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by caojing on 2017/12/22.
 */

public interface ITMSApi {

    @FormUrlEncoded
    @POST("upload")
    Call<UploadResult> upload(@Field("request") UploadReq request, @Field("accessToken") String
            accessToken);

    @FormUrlEncoded
    @POST("query")
    Call<AgreeResult> query(@Field("accessToken") String accessToken);
}
