package com.crejaud.jrejaud.cleverobjects.Server;

import android.content.Context;

import com.github.jrejaud.storage.ModelAndKeyStorage;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by Jordan on 6/11/2015.
 */
public class SmartThingsRESTHandler {
    private final static String TAG = "SmartThingsRESTHandler";

    private static SmartThingsRESTHandler ourInstance = new SmartThingsRESTHandler();

    public static SmartThingsRESTHandler getInstance() {
        return ourInstance;
    }

    private SmartThingsService smartThingsService;

    public boolean hasSwitches;
    public boolean hasLocks;

    //todo Need to hide this all sneaky-like
//    private String authenticationToken = "e3ef8674-d304-4c41-aa9d-f43d336feaf3";
//    private String endpointURI = "68345134-4dc4-4005-9c6d-cedfa4c48d7f";


    private SmartThingsRESTHandler() {
    }


    public SmartThingsService getSmartThingsService(Context context, final String authToken, String endpointURL) {

        //Record Auth token and endpointURL via MP (to make sure they are not null)
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Content-Type","application/json");
                request.addHeader("Authorization", "Bearer " + authToken);
            }
        };
        RestAdapter SmartThingsRestAdapter = new RestAdapter.Builder()
                .setEndpoint(endpointURL)
                .setRequestInterceptor(requestInterceptor)
                .build();
        smartThingsService = SmartThingsRestAdapter.create(SmartThingsService.class);
        return smartThingsService;
    }

}
