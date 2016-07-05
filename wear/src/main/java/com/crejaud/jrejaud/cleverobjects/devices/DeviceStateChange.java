package com.crejaud.jrejaud.cleverobjects.devices;

import com.github.jrejaud.WearSocket;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.DeviceStateChangeMessage;
import com.github.jrejaud.values.Values;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jrejaud on 7/13/15.
 */
public class DeviceStateChange {
    public void updateDeviceState(int deviceNumber, String action) {
        //Send message with device ID to phone
        JSONObject message = new JSONObject();
        try {
            message.put(DeviceStateChangeMessage.TYPE, DeviceStateChangeMessage.DEVICE_ID);
            message.put(DeviceStateChangeMessage.DATA, SmartThingsModelManager.getDevices().get(deviceNumber).getId());
            message.put(DeviceStateChangeMessage.ACTION, action);
            WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH, message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
