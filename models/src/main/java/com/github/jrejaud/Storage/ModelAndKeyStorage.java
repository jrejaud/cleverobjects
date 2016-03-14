package com.github.jrejaud.Storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.jrejaud.models.Device;
import com.github.jrejaud.values.Values;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jordan on 6/23/2015.
 */
public class ModelAndKeyStorage {
    private static ModelAndKeyStorage ourInstance = new ModelAndKeyStorage();

    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor sharedPreferencesEditor = null;
    private String preferenceKey = "com.jrejaud.cleverobjects.PREFKEY";
    private String devicesKey = "com.jrejaud.cleverobjects.DEVICES";
    private String phrasesKey = "com.jrejaud.cleverobjects.PHRASES";
    public static String clientIDKey = "com.jrejaud.cleverobjects.CLIENT_ID";
    public static String clientSecretKey = "com.jrejaud.cleverobjects.CLIENT_SECRET";
    public static String authTokenKey = "com.jrejaud.cleverobjects.AUTH_TOKEN";
    public static String endpointURIKey = "com.jrejaud.cleverobjects.ENDPOINT_URI";
    private String[] dataKeys = {clientIDKey,clientSecretKey,authTokenKey,endpointURIKey};

    public static ModelAndKeyStorage getInstance() {
        return ourInstance;
    }

    private ModelAndKeyStorage() {
    }

    public void storeDevices(Context context, List<Device> devices) {
        setupSharedPreferences(context);
        String jsonDevices = new Gson().toJson(devices);
        sharedPreferencesEditor.putString(devicesKey,jsonDevices);
        sharedPreferencesEditor.commit();
    }

    public List<Device> getStoredDevices(Context context) {
        setupSharedPreferences(context);
        String jsonDevices = sharedPreferences.getString(devicesKey, null);
        if (jsonDevices==null) {
            return null;
        }
        List<Device> devices = new Gson().fromJson(jsonDevices,new TypeToken<List<Device>>() {}.getType());
        return devices;
    }

    private void setupSharedPreferences(Context context) {
        if (sharedPreferences==null) {
            sharedPreferences = context.getSharedPreferences(preferenceKey,Context.MODE_PRIVATE);
            sharedPreferencesEditor = sharedPreferences.edit();
        }

    }

    public List<String> getStoredPhrases(Context context) {
        setupSharedPreferences(context);
        Set<String> phraseSet = sharedPreferences.getStringSet(phrasesKey,null);
        if (phraseSet==null) {
            return null;
        }
        List<String> phrases = new ArrayList<>(phraseSet);
        return phrases;
    }

    public void storePhrases(Context context, List<String> phrases) {
        setupSharedPreferences(context);
        Set<String> phrasesSet = new HashSet<>(phrases);
        sharedPreferencesEditor.putStringSet(phrasesKey, phrasesSet);
        sharedPreferencesEditor.commit();
    }

    public void storeData(Context context, String key, String data) {
        setupSharedPreferences(context);
        sharedPreferencesEditor.putString(key, data);
        sharedPreferencesEditor.commit();
    }

    public String getData(Context context, String key) {
        setupSharedPreferences(context);
        return sharedPreferences.getString(key,null);
    }
}
