package com.github.jrejaud.models;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by jrejaud on 7/4/16.
 */
public class SmartThingsDataContainer {

    @Expose
    List<Device> devices;

    @Expose
    List<String> phrases;

    public SmartThingsDataContainer() {
    }

    public SmartThingsDataContainer(List<Device> devices, List<String> phrases) {
        this.devices = devices;
        this.phrases = phrases;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public List<String> getPhrases() {
        return phrases;
    }


}
