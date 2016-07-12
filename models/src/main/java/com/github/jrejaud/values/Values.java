package com.github.jrejaud.values;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by jrejaud on 6/29/15.
 */
public class Values {
    public static final String TAG = "CleverObjects";
    public static final String MESSAGE_PATH = "CleverObjects";
    public static final String DATA_PATH = "/CleverObjects";
    public static final String MODEL_KEY = "CleverObjects_Model";
    public static final String PHRASES_KEY = "CleverObjects_Phrases";

    public static final HashMap<String,Type> DATA_KEYS = new HashMap<>();

    public static final String WEAR_CAPABILITY = "clever_objects";

    //Send this message when you want the wear app to delete its local keys
    public static final String DELETE_KEY = "CleverObjects_Delete";

    //Message sent from wear to phone (asking for updated device data)
    public static final String REQUEST_DATA = "REQUEST_DATA";

    //Reply from the phone to the wear if there is no data for the updated device data request
    public static final String NO_DATA = "NO_DATA";
}
