package com.example.jordan.retrofittest.Storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jordan.retrofittest.Models.Device;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jordan on 6/23/2015.
 */
public class ModelStorage {
    private static ModelStorage ourInstance = new ModelStorage();

    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor sharedPreferencesEditor = null;
    private String preferenceKey = "com.jrejaud.cleverobjects.PREFKEY";
    private String devicesKey = "com.jrejaud.cleverobjects.DEVICES";
    private String phrasesKey = "com.jreajud.cleverobjects.PHRASES";

    public static ModelStorage getInstance() {
        return ourInstance;
    }

    private ModelStorage() {
    }

    public void storeDevices(Context context, List<Device> devices) {
        if (sharedPreferences==null) {
            setupSharedPreferences(context);
        }
        String jsonDevices = new Gson().toJson(devices);
        sharedPreferencesEditor.putString(devicesKey,jsonDevices);
        sharedPreferencesEditor.commit();
    }

    public List<Device> getStoredDevices(Context context) {
        if (sharedPreferences==null) {
            setupSharedPreferences(context);
        }
        String jsonDevices = sharedPreferences.getString(devicesKey, null);
        if (jsonDevices==null) {
            return null;
        }
        List<Device> devices = new Gson().fromJson(jsonDevices,new TypeToken<List<Device>>() {}.getType());
        return devices;
    }

    private void setupSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(preferenceKey,Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    public List<String> getStoredPhrases(Context context) {
        if (sharedPreferences==null) {
            setupSharedPreferences(context);
        }
        Set<String> phraseSet = sharedPreferences.getStringSet(phrasesKey,null);
        if (phraseSet==null) {
            return null;
        }
        List<String> phrases = new ArrayList<>(phraseSet);
        return phrases;
    }

    public void storePhrases(Context context, List<String> phrases) {
        Set<String> phrasesSet = new HashSet<>(phrases);
        sharedPreferencesEditor.putStringSet(phrasesKey, phrasesSet);
        sharedPreferencesEditor.commit();
    }
}
