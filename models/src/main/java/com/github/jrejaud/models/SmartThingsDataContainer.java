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
    List<Phrase> phrases;

    public SmartThingsDataContainer() {
    }

    public SmartThingsDataContainer(List<Device> devices, List<Phrase> phrases) {
        this.devices = devices;
        this.phrases = phrases;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public void setPhrases(List<Phrase> phrases) {
        this.phrases = phrases;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public List<Phrase> getPhrases() {
        return phrases;
    }


}
