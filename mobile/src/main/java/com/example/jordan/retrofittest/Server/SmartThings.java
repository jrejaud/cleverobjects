package com.example.jordan.retrofittest.Server;

import android.util.Log;

import com.example.jordan.retrofittest.Models.Device;
import com.example.jordan.retrofittest.Models.SmartThingsModelManager;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

/**
 * Created by Jordan on 6/14/2015.
 */
public class SmartThings {

    private final String TAG = "CleverObjects2.0";
    private SmartThingsService smartThingsService;

    private static SmartThings ourInstance = new SmartThings();

    public static SmartThings getInstance() {
        return ourInstance;
    }

    private SmartThings() {
        smartThingsService = SmartThingsRESTHandler.getInstance().getSmartThingsService();
    }

    public void getDevices(Callback callback) {
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
        String typePath = getTypePath(device.getType());
        if (typePath==null) {
            //TODO Log error here, device is not a known type!
            return;
        }
        smartThingsService.setDevice(device.getType(), typePath, command, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                //TODO check that response is 200 (that means it worked)
                Log.d(TAG, device.getLabel() + " has been set " + command);
                //TODO remember to have continuis updateDeviceState running!
            }

            @Override
            public void failure(RetrofitError error) {

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
        smartThingsService.sayPhrase(phrase, new Callback() {
            @Override
            public void success(Object o, Response response) {
                //TODO phrase was successful
                Log.d(TAG,"Phrase: "+phrase+" was executed");
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO phrase was not successful
                Log.d(TAG, String.valueOf(error));
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
