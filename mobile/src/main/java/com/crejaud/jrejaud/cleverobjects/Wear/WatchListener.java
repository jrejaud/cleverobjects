package com.crejaud.jrejaud.cleverobjects.Wear;

import android.content.Context;
import android.util.Log;

import com.crejaud.jrejaud.cleverobjects.Server.SmartThings;
import com.github.jrejaud.WearSocket;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.Phrase;
import com.github.jrejaud.models.SmartThingsDataContainer;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.values.DeviceStateChangeMessage;
import com.github.jrejaud.values.Values;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by jrejaud on 7/5/15.
 */
public class WatchListener extends WearableListenerService {

    private Context context;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        context = getApplicationContext();
        String path = messageEvent.getPath();
        if (!path.equals(Values.MESSAGE_PATH)) {
            return;
        }

        //See if this is a request for devices
        if (new String(messageEvent.getData()).equals(Values.REQUEST_DATA)) {

            //Setup WearSocket
            WearSocket.getInstance().setupAndConnect(context, Values.WEAR_CAPABILITY, new WearSocket.onErrorListener() {
                @Override
                public void onError(Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });


            //Get the devices from the DB
            RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).build();
            Realm.setDefaultConfiguration(realmConfig);

            // Get a Realm instance for this thread
            Realm realm = Realm.getDefaultInstance();

            //Get devices and phrases from DB
            RealmResults<Device> devices = realm.where(Device.class).findAll();
            RealmResults<Phrase> phrases = realm.where(Phrase.class).findAll();

            //If there are devices
            if (devices!=null&&phrases.size()>0) {
                //Create SmartThingData Container
                SmartThingsDataContainer smartThingsDataContainer = new SmartThingsDataContainer(devices,phrases);

                //Convert DataContainer to JSON
                Gson gson = new Gson();
                String jsonData = gson.toJson(smartThingsDataContainer);

                //Send the DataContainer JSON String to the phone
                WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH,jsonData);
            } else {
                //If there are no devices, just send a NO DATA message
                WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH,Values.NO_DATA);
                MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(this,"d09bbd29f9af4459edcacbad0785c4c0");
                mixpanelAPI.track("No Data Message Sent",null);
            }



        } else {
            //This is a command message
            String messageString = new String(messageEvent.getData());
            try {
                JSONObject message = new JSONObject(messageString);
                if (message.get(DeviceStateChangeMessage.TYPE).equals(DeviceStateChangeMessage.PHRASE)) {
                    sayPhrase(message.getString(DeviceStateChangeMessage.DATA));
                    MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(this,"d09bbd29f9af4459edcacbad0785c4c0");
                    mixpanelAPI.track("User Say Phrase:", message);
                }
                else if (message.get(DeviceStateChangeMessage.TYPE).equals(DeviceStateChangeMessage.DEVICE_ID)) {
                    changeDeviceState(message.getString(DeviceStateChangeMessage.DATA),message.getString(DeviceStateChangeMessage.ACTION));
                    MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(this,"d09bbd29f9af4459edcacbad0785c4c0");
                    mixpanelAPI.track("User Change Device State:", message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Other message
                MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(this,"d09bbd29f9af4459edcacbad0785c4c0");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Stored message: ",messageString);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                mixpanelAPI.track("User Stored:", jsonObject);
            }
        }

        super.onMessageReceived(messageEvent);
    }

    //Move this to another method...
    private void changeDeviceState(String id, String state) {
        SmartThingsModelManager.setDevices(ModelAndKeyStorage.getInstance().getStoredDevices(context));
        Device device = SmartThingsModelManager.getDeviceByID(id);
        if (device!=null) {
            Log.d(Values.TAG, "Setting " + device + " state to: " + state);
            SmartThings smartThings = SmartThings.getInstance();
            smartThings.setup(context);
            smartThings.setDeviceState(device, state);
        }
    }

    private void sayPhrase(String phrase) {
        SmartThingsModelManager.setPhrases(ModelAndKeyStorage.getInstance().getStoredPhrases(context));
        if (!SmartThingsModelManager.getPhrases().contains(phrase)) {
            Log.e(Values.TAG,"Phrase is not contained in Model manager!");
            return;
        }
        Log.d(Values.TAG,"Saying phrase: "+phrase);
        SmartThings smartThings = SmartThings.getInstance();
        smartThings.setup(context);
        smartThings.sayPhrase(phrase);
    }
}
