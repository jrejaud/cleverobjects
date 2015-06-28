package com.example.jordan.retrofittest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.jordan.retrofittest.Communication.AndroidWearMessenger;
import com.example.jordan.retrofittest.Models.Device;
import com.example.jordan.retrofittest.Models.SmartThingsModelManager;
import com.example.jordan.retrofittest.Server.SmartThings;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PhoneActivity extends ActionBarActivity implements AndroidWearMessenger.AndroidWearMessageReceivedInterface {

    private final String TAG = "/CleverObjects";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        final AndroidWearMessenger androidWearMessenger = AndroidWearMessenger.getInstance();
        androidWearMessenger.setupAndConnect(this);
        androidWearMessenger.startMessageListener(this, TAG);

        SmartThings.getInstance().getDevices(new Callback() {
            @Override
            public void success(Object o, Response response) {
                androidWearMessenger.updateDataItem(TAG, "key225", SmartThingsModelManager.getInstance().getDevices());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        //Update Phrases
        SmartThings.getInstance().getPhrases(new Callback() {
            @Override
            public void success(Object o, Response response) {
                androidWearMessenger.updateDataItem(TAG,"phrases",SmartThingsModelManager.getInstance().getDevices());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });


    }

    @Override
    public void messageReceived(String path, String message) {
        Log.d(TAG, "Message received: " + message);
        changeDeviceState(message,"toggle");

    }

    private void changeDeviceState(String id, String state) {
        List<Device> devices = SmartThingsModelManager.getInstance().getDevices();
        for (Device device :devices) {
            if (device.getId().equals(id)) {
                SmartThings.getInstance().setDeviceState(device,state);
                Log.d(TAG,"Setting "+device+" state to: "+state);
                return;
            }
        }
    }
}

