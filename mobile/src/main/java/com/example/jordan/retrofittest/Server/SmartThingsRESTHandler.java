package com.example.jordan.retrofittest.Server;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by Jordan on 6/11/2015.
 */
public class SmartThingsRESTHandler {
    private static SmartThingsRESTHandler ourInstance = new SmartThingsRESTHandler();

    public static SmartThingsRESTHandler getInstance() {
        return ourInstance;
    }

    private SmartThingsService smartThingsService;

    public boolean hasSwitches;
    public boolean hasLocks;

    //Need to hide this all sneaky-like
    private String authenticationToken = "e3ef8674-d304-4c41-aa9d-f43d336feaf3";
    private String endpointURI = "68345134-4dc4-4005-9c6d-cedfa4c48d7f";
    private String SmartThingsEndpoint = "https://graph.api.smartthings.com/api/smartapps/installations/"+endpointURI;

    private SmartThingsRESTHandler() {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Content-Type","application/json");
                request.addHeader("Authorization", "Bearer " + authenticationToken);
            }
        };
        RestAdapter SmartThingsRestAdapter = new RestAdapter.Builder()
                .setEndpoint(SmartThingsEndpoint)
                .setRequestInterceptor(requestInterceptor)
                .build();
        smartThingsService = SmartThingsRestAdapter.create(SmartThingsService.class);
    }


    public SmartThingsService getSmartThingsService() {
        return smartThingsService;
    }

    public void setSmartThingsService(SmartThingsService smartThingsService) {
        this.smartThingsService = smartThingsService;
    }
}
