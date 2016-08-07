package com.crejaud.jrejaud.cleverobjects;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.jrejaud.WearSocket;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.SmartThingsDataContainer;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.values.Values;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by jrejaud on 7/5/15.
 */
public class PhoneListener extends WearableListenerService {

    private boolean hasBeenInitialized = false;

    public void initializeListener() {
        Values.DATA_KEYS.put(Values.MODEL_KEY, new TypeToken<List<Device>>() {
        }.getType());
        Values.DATA_KEYS.put(Values.PHRASES_KEY, new TypeToken<List<String>>(){}.getType());
        hasBeenInitialized = true;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (!hasBeenInitialized) {initializeListener();}
        String path = messageEvent.getPath();
        Log.d(Values.TAG, "New onMessageReceived: " + path + " message:" + new String(messageEvent.getData()));
        if (path.equals(Values.MESSAGE_PATH)) {
            messageReceived(path, new String(messageEvent.getData()));
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Timber.d("On Data Changed");
        if (!hasBeenInitialized) {initializeListener();}
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                //DataItem Changed
                DataItem item = dataEvent.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                Timber.d("Data map:"+dataMap.toString());
                Set<String> keys = dataMap.keySet();
                for (String key : keys) {
                    Timber.d("Key: "+key);
                    String data = dataMap.getString(key);
                    SmartThingsDataContainer smartThingsDataContainer = new Gson().fromJson(data,SmartThingsDataContainer.class);
//                    dataChanged(key,smartThingsDataContainer);
                }
            }
//            DataItem dataItem = dataEvent.getDataItem();
//            if (dataItem.getUri().getPath().compareTo(Values.DATA_PATH) == 0) {
//                DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
//                Set<String> keys = dataMap.keySet();
//                for (String key : keys) {
//                    String data = dataMap.getString(key);
//                    Gson gson = new Gson();
//                    if (!Values.DATA_KEYS.containsKey(key)) {
//                        Log.e(Values.TAG, key + " key not associated to a datatype, please setKeyDataType");
//                        return;
//                    }
//                    Object object = gson.fromJson(data, Values.DATA_KEYS.get(key));
//                    dataChanged(key, object);
//
//                }
//            }
        }
    }

    public void messageReceived(String path, String message) {
        //If this is a delete message, then wipe everything
        if (message.equals(Values.DELETE_KEY)) {
            ModelAndKeyStorage.getInstance().storePhrases(this, null);
            ModelAndKeyStorage.getInstance().storeDevices(this, null);
            stopApp(false);
        }
    }

//    public void dataChanged(String key, Object data) {
//        Log.d(Values.TAG, "Main data change method reached");
//
//        //Use the key to determine how to cast the bloody object
////        if (key.equals(Values.MODEL_KEY)) {
////            if (data==null) {
////
////                //If data is null, then clear the locally stored key
////                ModelAndKeyStorage.getInstance().storeDevices(this, null);
////            } else {
////                SmartThingsDataContainer smartThingsDataContainer = (SmartThingsDataContainer) data;
////
////                //Store devices
////                ModelAndKeyStorage.getInstance().storeDevices(this,smartThingsDataContainer.getDevices());
////
////                //Store phrases
////                ModelAndKeyStorage.getInstance().storePhrases(this, smartThingsDataContainer.getPhrases());
////                String storedData="";
////
////                for (Device device: smartThingsDataContainer.getDevices()) {
////                    Log.d(Values.TAG,"Received new device: "+device.getType()+" "+device.getLabel());
////                    storedData=storedData+device.getLabel()+", ";
////                }
////
////                //Store phrases
////                ModelAndKeyStorage.getInstance().storePhrases(this, smartThingsDataContainer.getPhrases());
////
////                for (String phrase : smartThingsDataContainer.getPhrases()) {
////                    Log.d(Values.TAG,"Received new phrase: "+phrase);
////                    storedData=storedData+phrase+", ";
////                }
////                stopApp(false);
//            }
////            //Stop a current execution of the app
////            //This way when the user opens it again, it will fetch the devices again.
//            }
//    }
    private void stopApp(boolean restart) {
        //Then restart the app
        Intent intent = new Intent(this,WatchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(WatchActivity.STOP_APP,!restart);
        startActivity(intent);
    }

}
