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
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;

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

    public void getDevices(final Subscriber<List<Device>> devicesSubscriber) {
        //TODO Does this crap even need to be here?
        if (smartThingsService==null) {
            Toast.makeText(context,"Please login to SmartThings",Toast.LENGTH_SHORT).show();
            return;
        }
        Observable.combineLatest(smartThingsService.getSwitchesObservable(), smartThingsService.getLocksObservable(), new Func2<List<Device>, List<Device>, List<Device>>() {
            @Override
            public List<Device> call(List<Device> switches, List<Device> locks) {
                List<Device> devices = new ArrayList<>();
                devices.addAll(switches);
                devices.addAll(locks);
                return devices;
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                devicesSubscriber.onError(throwable);
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                //
            }
        }).subscribe(new Action1<List<Device>>() {
            @Override
            public void call(List<Device> devices) {
                devicesSubscriber.onNext(devices);
            }
        });
    }

    public void setDeviceState(final Device device, final String command) {


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

    public void getPhrases(final Subscriber<List<String>> listSubscriber) {
        smartThingsService.getPhrases(new Callback<List<String>>() {
            @Override
            public void success(List<String> phrases, Response response) {
                listSubscriber.onNext(phrases);
            }

            @Override
            public void failure(RetrofitError error) {
                listSubscriber.onError(error);
            }
        });
    }

}
