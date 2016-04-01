package com.github.jrejaud.models;

import android.util.Log;

import com.github.jrejaud.values.Values;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jordan on 6/11/2015.
 */
public class SmartThingsModelManager {
    private static SmartThingsModelManager ourInstance = new SmartThingsModelManager();

    public static SmartThingsModelManager getInstance() {
        return ourInstance;
    }

    private SmartThingsModelManager() {
    }

    private List<Device> Devices = new ArrayList<>();
    private List<String> Phrases = new ArrayList<>();

    public List<Device> getDevices() {
        return Devices;
    }

    public List<String> getPhrases() {
        return Phrases;
    }

    public void setPhrases(List<String> phrases) {
        this.Phrases = phrases;
    }

    public void setDevices(List<Device> devices) { this.Devices = devices; }

    public Device getDeviceByID(String id) {
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
