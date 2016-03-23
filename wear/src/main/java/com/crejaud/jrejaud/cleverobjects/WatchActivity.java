package com.crejaud.jrejaud.cleverobjects;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                setupWearSocket();

                setupUIElements();

                updateUI();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Update it every time the user looks at the app again (regardless of if they destroy or stop the app)
        retrieveModel();
    }

    @Override
    protected void onDestroy() {
        try {
            WearSocket.getInstance().disconnect();
        } catch (NullPointerException ex) {
            Log.e("TAG","GoogleAPIClient must not be null, random bug",ex);
        }
        super.onDestroy();
    }

    private void setupWearSocket() {
        WearSocket wearSocket = WearSocket.getInstance();
        wearSocket.setupAndConnect(context, Values.WEAR_CAPABILITY, new WearSocket.onErrorListener() {
            @Override
            public void onError(Throwable throwable) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"You need to have a phone paired to use CleverObjects",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void retrieveModel() {
        boolean modelEmpty = true;
        if (ModelAndKeyStorage.getInstance().getStoredDevices(context) != null) {
            SmartThingsModelManager.getInstance().setDevices(ModelAndKeyStorage.getInstance().getStoredDevices(context));
            modelEmpty = false;
        }
        //This is test model
        //else {
//            List<Device> fakeList = new ArrayList<>();
//            Device fakeLock = new Device();
//            fakeLock.setLabel("Lock");
//            fakeLock.setId("LockID");
//            fakeLock.setType(Device.LOCK);
//            fakeList.add(fakeLock);
//            Device fakeSwitch = new Device();
//            fakeSwitch.setLabel("Switch");
//            fakeSwitch.setId("SwitchID");
//            fakeSwitch.setType(Device.SWITCH);
//            fakeList.add(fakeSwitch);
//            SmartThingsModelManager.getInstance().setDevices(fakeList);
//        }
        if (ModelAndKeyStorage.getInstance().getStoredPhrases(context)!=null) {
            SmartThingsModelManager.getInstance().setPhrases(ModelAndKeyStorage.getInstance().getStoredPhrases(context));
            modelEmpty = false;
        }
        //This is test phrases
//        else {
//            //Set up phrases
//            List<String> phrases = new ArrayList<>();
//            phrases.add("Good morning!");
//            phrases.add("Good evening!");
//            SmartThingsModelManager.getInstance().setPhrases(phrases);
//            modelEmpty = false;
//        }


        if (modelEmpty) {
            //If the model is empty (there are no devices or phrases), user needs to setup the app first
            promptUserToSetupOnPhoneFirst();
        }
    }

    private void setupUIElements() {
        //listView = (WearableListView) findViewById(R.id.actions_list_view);
        gridViewPager = (GridViewPager) findViewById(R.id.main_menu_grid_view);
    }

//    @Override
//    public void messageReceived(String path, String message) {
//        Log.d(Values.TAG, "Message: " + path + " " + message);
//    }
//
//    @Override
//    public void dataChanged(String key, Object data) {
//        Log.d(Values.TAG, "Main data change method reached");
//        //Use the key to determine how to cast the bloody object
//
//        if (key.equals(Values.MODEL_KEY)) {
//            ArrayList<Device> deviceArrayList = (ArrayList<Device>) data;
//            SmartThingsModelManager.getInstance().setDevices(deviceArrayList);
//            ModelAndKeyStorage.getInstance().storeDevices(context, SmartThingsModelManager.getInstance().getDevices());
//            for (Device device: deviceArrayList) {
//                Log.d(Values.TAG,"Found device: "+device.getType()+" "+device.getLabel());
//            }
//        }
//        if (key.equals(Values.PHRASES_KEY)) {
//            SmartThingsModelManager.getInstance().setPhrases((ArrayList<String>) data);
//            ModelAndKeyStorage.getInstance().storePhrases(context, SmartThingsModelManager.getInstance().getPhrases());
//        }
//
//        //todo do I need this here???
//        updateUI();
//    }

    private void updateUI() {
        DevicesGridAdapter devicesGridAdapter = new DevicesGridAdapter(context,SmartThingsModelManager.getInstance().getDevices());
        gridViewPager.setAdapter(devicesGridAdapter);
//        DeviceAdapter deviceAdapter = new DeviceAdapter(context,SmartThingsModelManager.getInstance().getDevices());
//        listView.setAdapter(deviceAdapter);
//        listView.setClickListener(new WearableListView.ClickListener() {
//            @Override
//            public void onClick(WearableListView.ViewHolder viewHolder) {
//                //Voice is pressed
//                int position = viewHolder.getPosition();
//                if (position == 0) {
//                    //Todo implement voice mode
//                } else if (position == 1) {
//                    Intent phraseActivityIntent = new Intent(context, PhrasesActivity.class);
//                    startActivity(phraseActivityIntent);
//                }
//
//                //A device is pressed
//                else if (position > 1) {
//                    WearSocket.getInstance().sendMessage(Values.TAG, SmartThingsModelManager.getInstance().getDevices().get(position - 2).getId());
//                }
//            }
//
//            @Override
//            public void onTopEmptyRegionClick() {
//
//            }
//        });
    }

    private void promptUserToSetupOnPhoneFirst() {
        Toast.makeText(context,"Please set up CleverObjects on phone first",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode== DevicesGridAdapter.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            new VoiceRecognition(data,context);
        }
    }
}