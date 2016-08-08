package com.github.jrejaud.models;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by jrejaud on 7/4/16.
 */
public class SmartThingsDataContainer {

    @Expose
    List<DevicePOJO> devices;

    @Expose
    List<PhrasePOJO> phrases;

    public SmartThingsDataContainer() {
    }

    public SmartThingsDataContainer(List<DevicePOJO> devices, List<PhrasePOJO> phrases) {
        this.devices = devices;
        this.phrases = phrases;
    }

    public void setDevices(List<DevicePOJO> devices) {
        this.devices = devices;
    }

    public void setPhrases(List<PhrasePOJO> phrases) {
        this.phrases = phrases;
    }

    public List<DevicePOJO> getDevices() {
        return devices;
    }

    public List<PhrasePOJO> getPhrases() {
        return phrases;
    }
}
