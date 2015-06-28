package com.example.jordan.retrofittest.Models;

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

    private  List<Device> Devices = new ArrayList<>();
    private List<String> Phrases = new ArrayList<>();

    public List<Device> getDevices() {
        return Devices;
    }

    public void setDevices(List<Device> devices) {
        this.Devices = devices;
    }

    public List<String> getPhrases() {
        return Phrases;
    }

    public void setPhrases(List<String> phrases) {
        this.Phrases = phrases;
    }




}
