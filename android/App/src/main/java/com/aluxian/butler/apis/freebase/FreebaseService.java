package com.aluxian.butler.apis.freebase;

import com.aluxian.butler.BuildConfig;
import com.aluxian.butler.utils.Constants;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Interface service for communication with the Freebase API
 */
public interface FreebaseService {

    @GET("/search?limit=1&spell=always")
    SearchResponse search(@Query("query") String query, @Query("filter") String filter);

    @GET("/topic/{id}")
    LookupResult lookup(@Path("id") String id);

    /** Service instance */
    public static FreebaseService api = new RestAdapter.Builder()
            .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
            .setEndpoint(Constants.FREEBASE_API_URL)
            .setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("key", Constants.FREEBASE_API_KEY);
                }
            })
            .build().create(FreebaseService.class);

}
