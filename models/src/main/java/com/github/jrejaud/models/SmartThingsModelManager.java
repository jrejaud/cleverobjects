package com.github.jrejaud.models;

import android.util.Log;

import com.github.jrejaud.values.Values;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jordan on 6/11/2015.
 */
public class SmartThingsModelManager {

    private static List<Device> Devices = new ArrayList<>();
    private static List<String> Phrases = new ArrayList<>();

    public static List<Device> getDevices() {
        return Devices;
    }

    public static List<String> getPhrases() {
        return Phrases;
    }

    public static void setPhrases(List<String> phrases) {
        SmartThingsModelManager.Phrases = phrases;
    }

    public static void setDevices(List<Device> devices) { SmartThingsModelManager.Devices = devices; }

    public static Device getDeviceByID(String id) {
        //If there are no devices, it means that there is no auth key saved on the phone.
        if (Devices==null) {
            return null;
        }
        for (Device device :Devices) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }


}
