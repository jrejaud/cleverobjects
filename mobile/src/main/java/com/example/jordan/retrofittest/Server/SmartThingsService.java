package com.example.jordan.retrofittest.Server;


import com.example.jordan.retrofittest.Models.Device;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by Jordan on 6/9/2015.
 */
public interface SmartThingsService {

    @GET("/switches")
    void getSwitches(Callback<List<Device>> cb);

    @GET("/switches")
    Observable<List<Device>> getSwitchesObservable();

    @GET("/locks")
    void getLocks(Callback<List<Device>> cb);

    @GET("/locks")
    Observable<List<Device>> getLocksObservable();

    @GET("/{type}/{id}/{command}")
    void setDevice(@Path("type") String type, @Path("id") String id, @Path("command") String command, Callback<Object> cb);

    @GET("/phrases")
    void getPhrases(Callback<List<String>> cb);

    @GET("/phrases/{phraseName}")
    void sayPhrase(@Path("phrasename") String phraseName, Callback cb);


}
