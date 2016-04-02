package com.crejaud.jrejaud.cleverobjects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.Toast;

import com.crejaud.jrejaud.cleverobjects.devices.DevicesGridAdapter;
import com.crejaud.jrejaud.cleverobjects.voicerecognition.VoiceRecognition;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.Values;
import com.github.jrejaud.wear_socket.WearSocket;


public class WatchActivity extends Activity {

    public static Context context;
    private GridViewPager gridViewPager;
    private DevicesGridAdapter devicesGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TAG","onCreate");
        super.onCreate(savedInstanceState);
        context = this;
        if (isModelEmpty()) {
            Log.d("TAG","model is empty!");
            promptUserToSetupOnPhoneFirst();
        } else {
            Log.d("TAG","model is not empty!");
        }
        setContentView(R.layout.activity_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                setupWearSocket();
                setupUIElements();
                updateModel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "Resume: Checking if model is empty");
        if (isModelEmpty()) {
            Log.d("TAG","Resume: Model is empty");
            promptUserToSetupOnPhoneFirst();
        } else {
            Log.d("TAG","Resume: Model is not empty");
        }
    }

    @Override
    protected void onDestroy() {
        try {
            WearSocket.getInstance().disconnect();
        } catch (NullPointerException ex) {
            Log.e("TAG",ex.toString());
        }
        super.onDestroy();
    }

    private void setupWearSocket() {
        WearSocket wearSocket = WearSocket.getInstance();
        wearSocket.setupAndConnect(context, Values.WEAR_CAPABILITY, new WearSocket.onErrorListener() {
            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "You need to have a phone paired to use CleverObjects", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void updateModel() {

        if (isModelEmpty()) {
            promptUserToSetupOnPhoneFirst();
        }

        if (ModelAndKeyStorage.getInstance().getStoredDevices(context) != null) {
            SmartThingsModelManager.setDevices(ModelAndKeyStorage.getInstance().getStoredDevices(context));
        }
        if (ModelAndKeyStorage.getInstance().getStoredPhrases(context)!=null) {
            SmartThingsModelManager.setPhrases(ModelAndKeyStorage.getInstance().getStoredPhrases(context));
        }

        devicesGridAdapter.notifyDataSetChanged();
    }

    private boolean isModelEmpty() {
        if (ModelAndKeyStorage.getInstance().getStoredDevices(context) == null && ModelAndKeyStorage.getInstance().getStoredPhrases(context) ==null) {
            return true;
        }

        return false;
    }

    private void setupUIElements() {
        gridViewPager = (GridViewPager) findViewById(R.id.main_menu_grid_view);
        devicesGridAdapter = new DevicesGridAdapter(context,SmartThingsModelManager.getDevices());
        gridViewPager.setAdapter(devicesGridAdapter);
    }

    private void promptUserToSetupOnPhoneFirst() {
        Toast.makeText(getBaseContext(),"Please set up CleverObjects on phone first",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode== DevicesGridAdapter.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            new VoiceRecognition(data,context);
        }
    }
}