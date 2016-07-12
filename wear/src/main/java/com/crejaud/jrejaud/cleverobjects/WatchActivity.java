package com.crejaud.jrejaud.cleverobjects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.Toast;

import com.crejaud.jrejaud.cleverobjects.devices.DevicesGridAdapter;
import com.crejaud.jrejaud.cleverobjects.voicerecognition.VoiceRecognition;
import com.github.jrejaud.WearSocket;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.SmartThingsDataContainer;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.Values;
import com.google.gson.Gson;

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

        //If the Phone listener received a delete message from the phone, it will restart this activity and set STOP_APP to true
        if (getIntent().getBooleanExtra(STOP_APP,false)) {
            finish();
            return;
        }

        //Setup debugging (if needed)
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        //Setup App
        setContentView(R.layout.activity_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //Setup WearSocket
                setupWearSocket();

                //Setup UI Elements
                setupUIElements();

                //If Device Model is Empty
                if (isDevicesModelEmpty()) {

                    //Show a message telling the user that app is updated
                    Timber.d("No local device model, checking mobile app");

                    //Start Message Listener
                    WearSocket.getInstance().startMessageListener(new WearSocket.MessageListener() {
                        @Override
                        public void messageReceived(String path, String message) {

                            if (path.equals(Values.MESSAGE_PATH)) {
                                Timber.d("Received mobile message: "+message);

                                //If there are no devices on phone
                                if (message.equals(Values.NO_DATA)) {
                                    //Show bad message
                                    Timber.d("No device data on the phone");
                                    promptUserToSetupOnPhoneFirst();
                                    return;
                                } else {
                                    //If there are devices on phone's message

                                    //Extract the device list via JSON
                                    SmartThingsDataContainer smartThingsDataContainer = new Gson().fromJson(message,SmartThingsDataContainer.class);

                                    //Save the devices locally
                                    ModelAndKeyStorage.getInstance().storeDevices(context,smartThingsDataContainer.getDevices());
                                    ModelAndKeyStorage.getInstance().storePhrases(context, smartThingsDataContainer.getPhrases());

                                    //Update Devices View
                                    updateDevicesView(smartThingsDataContainer.getDevices(),smartThingsDataContainer.getPhrases());
                                }
                            }
                        }
                    },Values.MESSAGE_PATH);

                    //Send a message to the app asking for updated devices
                    WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH,Values.REQUEST_DATA);
                    return;
                }

                //If device model is not empty, then update the devices view from the preferences
                updateDevicesView(getDevicesFromPreferences(),getPhrasesFromPreferences());
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

    private List<Device> getDevicesFromPreferences() {
        return ModelAndKeyStorage.getInstance().getStoredDevices(context);
    }

    private List<String> getPhrasesFromPreferences() {
        return ModelAndKeyStorage.getInstance().getStoredPhrases(context);
    }

    private void updateDevicesView(List<Device> deviceList, List<String> phrasesList) {

        if (deviceList.size()>1) {
            SmartThingsModelManager.setDevices(deviceList);
            devicesGridAdapter = new DevicesGridAdapter(context,deviceList);
        }

        if (phrasesList.size()>1) {
            SmartThingsModelManager.setPhrases(phrasesList);
        }

        gridViewPager.setAdapter(devicesGridAdapter);
    }
    

    /** */
    private boolean isDevicesModelEmpty()
    {
        List<Device> deviceList = ModelAndKeyStorage.getInstance().getStoredDevices(context);
        return deviceList == null;

    }

    private void setupUIElements() {
        gridViewPager = (GridViewPager) findViewById(R.id.main_menu_grid_view);
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