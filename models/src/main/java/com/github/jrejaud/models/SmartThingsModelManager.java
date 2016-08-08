package com.github.jrejaud.models;

import android.util.Log;

import com.github.jrejaud.values.Values;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jordan on 6/11/2015.
 */
public class SmartThingsModelManager {

    private static List<DevicePOJO> Devices = new ArrayList<>();
    private static List<PhrasePOJO> Phrases = new ArrayList<>();

    public static List<DevicePOJO> getDevices() {
        return Devices;
    }

    public static List<PhrasePOJO> getPhrases() {
        return Phrases;
    }

    public static void setPhrases(List<PhrasePOJO> phrases) {
        SmartThingsModelManager.Phrases = phrases;
    }

    public static void setDevices(List<DevicePOJO> devices) { SmartThingsModelManager.Devices = devices; }

//    public static Device getDeviceByID(String id) {
//        //If there are no devices, it means that there is no auth key saved on the phone.
//        if (Devices==null) {
//            return null;
//        }
//        for (Device device :Devices) {
//            if (device.getId().equals(id)) {
//                return device;
//            }
//        }
//        return null;
//    }


}
