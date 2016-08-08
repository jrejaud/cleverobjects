package com.crejaud.jrejaud.cleverobjects.voicerecognition;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.crejaud.jrejaud.cleverobjects.devices.DeviceStateChange;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.DevicePOJO;
import com.github.jrejaud.models.SmartThingsModelManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jrejaud on 7/7/15.
 */
public class VoiceRecognition {

    private static final String AND = "and";
    private static final String TOGGLE = "toggle";
    private static final String ON = "turnon";
    private static final String OFF = "turnoff";
    private static final String EVERYTHING = "everything";
    private static final String LOCK = "lock";
    private static final String UNLOCK = "unlock";

    private Context context;

    public VoiceRecognition(Intent data, Context context) {
        this.context = context;
        List<String> results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);
        String spokenText = results.get(0);
        String[] commands = getIndividualCommands(spokenText);
        for (String command : commands) {
            handleCommand(command);
        }
    }

    private String[] getIndividualCommands(String spokenText) {
        spokenText = spokenText.replace(" ","");

        spokenText = spokenText.toLowerCase();

        String[] commands = spokenText.split(AND);

        return commands;
    }



    private void handleCommand(String command) {
        String action;
        if (command.contains(TOGGLE)) {
            command = command.replace(TOGGLE, "");
            action = Device.TOGGLE;
        }
        else if (command.contains(ON)) {
            command = command.replace(ON, "");
            action = Device.ON;
        }
        else if (command.contains(OFF)) {
            command = command.replace(OFF, "");
            action = Device.OFF;
        }
        else if (command.contains(LOCK)) {
            command = command.replace(LOCK,"");
            action = Device.LOCK;
        }
        else if (command.contains(UNLOCK)) {
            command = command.replace(UNLOCK,"");
            action = Device.LOCK;
        }
        else {
            //todo error, no state change in this command
            return;
        }
        List<String> lowerCaseDeviceNames = deviceLabelsToLowerCase(SmartThingsModelManager.getDevices());
        if (lowerCaseDeviceNames.contains(command)) {
            int index = lowerCaseDeviceNames.indexOf(command);
            DeviceStateChange deviceStateChange = new DeviceStateChange();
            deviceStateChange.updateDeviceState(index,action);
            Toast.makeText(context, SmartThingsModelManager.getDevices().get(index).getLabel()+" "+action, Toast.LENGTH_SHORT).show();

        }
    }

    private List<String> deviceLabelsToLowerCase(List<DevicePOJO> devices) {
        ArrayList<String> lowerCaseList = new ArrayList<>();

        for (DevicePOJO device : devices) {
            String label = device.getLabel();
            label = label.replace(" ","");
            lowerCaseList.add(label.toLowerCase());
        }

        return lowerCaseList;
    }

}
