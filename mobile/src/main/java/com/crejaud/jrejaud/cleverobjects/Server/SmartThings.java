package com.crejaud.jrejaud.cleverobjects.Server;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.Values;
import com.github.jrejaud.wear_socket.WearSocket;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;

/**
 * Created by Jordan on 6/14/2015.
 */
public class SmartThings {

    private final String TAG = "CleverObjects2.0";
    private Context context;
    private SmartThingsService smartThingsService;

    private static SmartThings ourInstance = new SmartThings();

    public static SmartThings getInstance() {
        return ourInstance;
    }

    private SmartThings() {
    }

    public void setup(Context context) {
        this.context = context;
        smartThingsService = SmartThingsRESTHandler.getInstance().getSmartThingsService(context);
    }

    public void getDevices(Callback callback) {
        if (smartThingsService==null) {
            Toast.makeText(context,"Please login to SmartThings",Toast.LENGTH_SHORT).show();
            return;
        }
        Observable.combineLatest(smartThingsService.getSwitchesObservable(), smartThingsService.getLocksObservable(),
                (switches, locks) -> {
                List<Device> devices = new ArrayList<>();
                devices.addAll(switches);
                devices.addAll(locks);
                return devices;
            })
                .doOnError(throwable -> { Log.e(TAG, String.valueOf(throwable)); })
                .doOnCompleted(() -> callback.success(null, null))
                .subscribe(st_devices -> SmartThingsModelManager.getInstance().setDevices(st_devices));

    }

    public void setDeviceState(final Device device, final String command) {

//        ModelAndKeyStorage modelAndKeyStorage = ModelAndKeyStorage.getInstance();
//        final String authenticationToken = modelAndKeyStorage.getData(context,ModelAndKeyStorage.authTokenKey);
//        String endpointURI = modelAndKeyStorage.getData(context,ModelAndKeyStorage.endpointURIKey);

//        Log.d(TAG,"About to try to do something to device state...");
//        Log.d(TAG,"Device to be set: "+device.getLabel()+", "+device.getId());
//        Log.d(TAG,"Auth token: "+authenticationToken);
//        Log.d(TAG,"endpoint URI: "+endpointURI);

        String typePath = getTypePath(device.getType());
        if (typePath==null) {
            //TODO Log error here, device is not a known type!
            return;
        }
//        Log.d(TAG,"typePath: "+typePath);
//        Log.d(TAG,"command: "+command);
        smartThingsService.setDevice(typePath, device.getId(), command, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                //TODO check that response is 200 (that means it worked)
                String message = device.getLabel() + " has been set " + command;
                Log.d(TAG, message);
                //TODO remember to have continuis updateDeviceState running!
                //WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH,message);
            }

            @Override
            public void failure(RetrofitError error) {
                String message = "Error: "+device.getLabel()+" isn't responding!";
                Log.d(TAG, String.valueOf(error));
                //WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH,message);
            }
        });
    }

    private String getTypePath(String path) {
        if (path.equals(Device.SWITCH)) {
            return "switches";
        } else if (path.equals(Device.LOCK)) {
            return "locks";
        } else {
            return null;
        }
    }

    public void sayPhrase(final String phrase) {
        smartThingsService.sayPhrase(phrase, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                //TODO phrase was successful
                Log.d(TAG, "Phrase: " + phrase + " was executed");
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO phrase was not successful
                Log.d(TAG, String.valueOf(error));
                WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH, "Error: " + phrase + " didn't work");
            }
        });
    }

    public void getPhrases(final Callback callback) {
        smartThingsService.getPhrases(new Callback<List<String>>() {
            @Override
            public void success(List<String> phrases, Response response) {
                Log.d(TAG, "Phrases: " + String.valueOf(phrases));
                SmartThingsModelManager.getInstance().setPhrases(phrases);
                callback.success(null,null);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, String.valueOf(error));
            }
        });
    }
}
