package com.github.jrejaud.models;

import java.util.List;

/**
 * Created by jrejaud on 7/4/16.
 */
public class SmartThingsDataContainer {
    List<Device> devices;
    List<String> phrases;

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
