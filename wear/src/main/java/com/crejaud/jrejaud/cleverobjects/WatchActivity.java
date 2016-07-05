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
import com.github.jrejaud.WearSocket;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.Values;

import java.util.List;

import timber.log.Timber;


public class WatchActivity extends Activity {

    public static Context context;
    private GridViewPager gridViewPager;
    private DevicesGridAdapter devicesGridAdapter;
    public static final String STOP_APP = "STOP_APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        //If the Phone listener received a delete message from the phone, it will restart this activity and set STOP_APP to true
        if (getIntent().getBooleanExtra(STOP_APP,false)) {
            finish();
            return;
        }

        if (isModelEmpty()) {
            promptUserToSetupOnPhoneFirst();
            return;
        }

        setContentView(R.layout.activity_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                setupWearSocket();
                setupUIElements();
//                If the device model can't be found or has zero devices, you need to alert the user.
                if (!updateModel()) {
                    promptUserToSetupOnPhoneFirst();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("OnResume");
        Log.d("TAG", "Resume: Checking if model is empty");
//        if (isModelEmpty()) {
//            Log.d("TAG","Resume: Model is empty");
//            promptUserToSetupOnPhoneFirst();
//        } else {
//            Log.d("TAG","Resume: Model is not empty");
//        }
    }

    @Override
    protected void onDestroy() {
        try {
            WearSocket.getInstance().disconnect();
        } catch (Exception ex) {
            Timber.d(ex,"onDestroy Crash");
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

    private boolean updateModel() {

        if (isModelEmpty()) {
            promptUserToSetupOnPhoneFirst();
        }

        if (ModelAndKeyStorage.getInstance().getStoredDevices(context) != null) {
            List<Device> deviceList = ModelAndKeyStorage.getInstance().getStoredDevices(context);
            //If there are no devices stored, return null
            if (deviceList.size()<1) {
                return false;
            }
            SmartThingsModelManager.setDevices(deviceList);
            devicesGridAdapter = new DevicesGridAdapter(context,deviceList);
        } else {
            //Return false if you cannot get stored devices
            return false;
        }
        if (ModelAndKeyStorage.getInstance().getStoredPhrases(context)!=null) {
            SmartThingsModelManager.setPhrases(ModelAndKeyStorage.getInstance().getStoredPhrases(context));
        }

        gridViewPager.setAdapter(devicesGridAdapter);
        return true;
    }

    private boolean isModelEmpty() {
        return ModelAndKeyStorage.getInstance().getStoredDevices(context) == null;

    }

    private void setupUIElements() {
        gridViewPager = (GridViewPager) findViewById(R.id.main_menu_grid_view);
//        devicesGridAdapter = new DevicesGridAdapter(context,SmartThingsModelManager.getDevices());
//        gridViewPager.setAdapter(devicesGridAdapter);
    }

    private void promptUserToSetupOnPhoneFirst() {
        Toast.makeText(this,"Please set up CleverObjects on phone first",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode== DevicesGridAdapter.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            new VoiceRecognition(data,context);
        }
    }
}