package com.example.jordan.retrofittest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.example.jordan.retrofittest.Models.Device;
import com.example.jordan.retrofittest.Models.SmartThingsModelManager;
import com.example.jordan.retrofittest.Storage.ModelStorage;
import com.github.jrejaud.wear_socket.WearSocket;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


public class WatchActivity extends Activity implements WearSocket.DataListener, WearSocket.MessageListener {

    private final String TAG = "/CleverObjects";
    private Context context;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                WearSocket wearSocket = WearSocket.getInstance();
                wearSocket.setupAndConnect(context);
                wearSocket.startMessageListener(context, TAG);
                wearSocket.startDataListener(context, TAG);
                wearSocket.setKeyDataType("key225", new TypeToken<List<Device>>() {
                }.getType());
                gridView = (GridView) findViewById(R.id.clever_objects_gridview);

                if (ModelStorage.getInstance().getStoredDevices(context) != null) {
                    SmartThingsModelManager.getInstance().setDevices(ModelStorage.getInstance().getStoredDevices(context));
                }

                updateUI();
            }
        });
    }

    @Override
    public void messageReceived(String path, String message) {
        Log.d(TAG, "Message: " + path + " " + message);
    }

    @Override
    public void dataChanged(String key, Object data) {
        Log.d(TAG, "Main data change method reached");
        //Use the key to determine how to cast the bloody object

        if (key.equals("key223")) {
            ArrayList<Device> deviceArrayList = (ArrayList<Device>) data;
            SmartThingsModelManager.getInstance().setDevices(deviceArrayList);
            ModelStorage.getInstance().storeDevices(context, SmartThingsModelManager.getInstance().getDevices());
            for (Device device: deviceArrayList) {
                Log.d(TAG,"Found device: "+device.getType()+" "+device.getLabel());
            }
        }
        if (key.equals("phrases")) {
            SmartThingsModelManager.getInstance().setPhrases((ArrayList<String>) data);
            ModelStorage.getInstance().storePhrases(context, SmartThingsModelManager.getInstance().getPhrases());
        }

        updateUI();
    }

    private void updateUI() {
        DeviceAdapter deviceAdapter = new DeviceAdapter(context,SmartThingsModelManager.getInstance().getDevices());
        gridView.setAdapter(deviceAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //A device is pressed
                if (id>1) {
                    WearSocket.getInstance().sendMessage(TAG,SmartThingsModelManager.getInstance().getDevices().get(position-2).getId());
                }
            }
        });
    }
}