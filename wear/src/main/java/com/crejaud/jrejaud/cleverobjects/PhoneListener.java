package com.crejaud.jrejaud.cleverobjects;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.SmartThingsModelManager;
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

/**
 * Created by jrejaud on 7/5/15.
 */
public class PhoneListener extends WearableListenerService {

    private Context context;
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
        context = getApplicationContext();
        String path = messageEvent.getPath();
        Log.d(Values.TAG,"New onMessageReceived: "+path+" message:"+ new String(messageEvent.getData()));
        if (path.equals(Values.MESSAGE_PATH)) {
            messageReceived(path,new String(messageEvent.getData()));
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (!hasBeenInitialized) {initializeListener();}
        for (DataEvent dataEvent : dataEvents) {
            DataItem dataItem = dataEvent.getDataItem();
            if (dataItem.getUri().getPath().compareTo(Values.DATA_PATH) == 0) {
                DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                Set<String> keys = dataMap.keySet();
                for (String key : keys) {
                    String data = dataMap.getString(key);
                    Gson gson = new Gson();
                    if (!Values.DATA_KEYS.containsKey(key)) {
                        Log.e(Values.TAG, key + " key not associated to a datatype, please setKeyDataType");
                        return;
                    }
                    Object object = gson.fromJson(data, Values.DATA_KEYS.get(key));
                    dataChanged(key, object);

                }
            }
        }
    }

    public void messageReceived(String path, String message) {
        Log.d(Values.TAG, "Message: " + path + " " + message);
        //For now, only messages will be error messages
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    public void dataChanged(String key, Object data) {
        Log.d(Values.TAG, "Main data change method reached");

        //Use the key to determine how to cast the bloody object
        if (key.equals(Values.MODEL_KEY)) {
            ArrayList<Device> deviceArrayList = (ArrayList<Device>) data;
            ModelAndKeyStorage.getInstance().storeDevices(getApplicationContext(), SmartThingsModelManager.getInstance().getDevices());
            for (Device device: deviceArrayList) {
                Log.d(Values.TAG,"Received new device: "+device.getType()+" "+device.getLabel());
            }
        }
        if (key.equals(Values.PHRASES_KEY)) {
            List<String> phrases = (ArrayList<String>) data;
            for (String phrase : phrases) {
                Log.d(Values.TAG,"Received new phrase: "+phrase);
            }
            ModelAndKeyStorage.getInstance().storePhrases(context, SmartThingsModelManager.getInstance().getPhrases());
        }

        //Then reset the app!
        Intent intent = new Intent(getBaseContext(),WatchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }

}
