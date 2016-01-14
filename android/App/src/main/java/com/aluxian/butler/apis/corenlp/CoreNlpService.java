package com.aluxian.butler.apis.corenlp;

import com.aluxian.butler.BuildConfig;
import com.aluxian.butler.utils.Constants;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Interface service for communication with the CoreNlpServer API
 */
public interface CoreNlpService {

    @FormUrlEncoded
    @POST("/semgraph")
    List<String> getSemgraphs(@Field("text") String text);

    @FormUrlEncoded
    @POST("/sentences?limit=1")
    List<String> getSentences(@Field("text") String text);

    @FormUrlEncoded
    @POST("/sentiment")
    List<String> getSentiments(@Field("text") String text);

    @FormUrlEncoded
    @POST("/gender")
    List<List<String>> getGenders(@Field("text") String text);

    /** Service instance */
    public static CoreNlpService api = new RestAdapter.Builder()
            .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
            .setEndpoint(Constants.CORE_NLP_SERVER_URL)
            .build().create(CoreNlpService.class);

}
