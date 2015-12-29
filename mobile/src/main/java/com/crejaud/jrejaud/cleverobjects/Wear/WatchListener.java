package com.crejaud.jrejaud.cleverobjects.Wear;

import android.content.Context;
import android.util.Log;

import com.crejaud.jrejaud.cleverobjects.Server.SmartThings;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.values.DeviceStateChangeMessage;
import com.github.jrejaud.values.Values;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jrejaud on 7/5/15.
 */
public class WatchListener extends WearableListenerService {

    private Context context;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        context = getApplicationContext();
        String path = messageEvent.getPath();
        if (!path.equals(Values.MESSAGE_PATH)) {
            return;
        }

        String messageString = new String(messageEvent.getData());
        try {
            JSONObject message = new JSONObject(messageString);
            if (message.get(DeviceStateChangeMessage.TYPE).equals(DeviceStateChangeMessage.PHRASE)) {
                sayPhrase(message.getString(DeviceStateChangeMessage.DATA));

            }
            else if (message.get(DeviceStateChangeMessage.TYPE).equals(DeviceStateChangeMessage.DEVICE_ID)) {
                changeDeviceState(message.getString(DeviceStateChangeMessage.DATA),message.getString(DeviceStateChangeMessage.ACTION));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onMessageReceived(messageEvent);
    }

    //Move this to another method...
    private void changeDeviceState(String id, String state) {
        SmartThingsModelManager.getInstance().setDevices(ModelAndKeyStorage.getInstance().getStoredDevices(context));
        Device device = SmartThingsModelManager.getInstance().getDeviceByID(id);
        if (device!=null) {
            Log.d(Values.TAG, "Setting " + device + " state to: " + state);
            SmartThings smartThings = SmartThings.getInstance();
            smartThings.setup(context);
            smartThings.setDeviceState(device, state);
        }
    }

    private void sayPhrase(String phrase) {
        SmartThingsModelManager.getInstance().setPhrases(ModelAndKeyStorage.getInstance().getStoredPhrases(context));
        if (!SmartThingsModelManager.getInstance().getPhrases().contains(phrase)) {
            Log.e(Values.TAG,"Phrase is not contained in Model manager!");
            return;
        }
        Log.d(Values.TAG,"Saying phrase: "+phrase);
        SmartThings smartThings = SmartThings.getInstance();
        smartThings.setup(context);
        smartThings.sayPhrase(phrase);
    }
}
