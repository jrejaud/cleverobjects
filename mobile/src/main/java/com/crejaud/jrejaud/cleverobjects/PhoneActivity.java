package com.crejaud.jrejaud.cleverobjects;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crejaud.jrejaud.cleverobjects.Server.SmartThings;
import com.github.jrejaud.WearSocket;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.Phrase;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.values.Values;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import rx.Subscriber;
import timber.log.Timber;


public class PhoneActivity extends CleverObjectsActivity {

    private Context context;
    private TextView middleText;
    private Button setupButton;
    private Button unpairButton;
    private FrameLayout mainImageFrame;
    private ListView devicesListView;
    public static final String ENDPOINT_URL = "ENDPOINT_URL";
    private ImageView mainImage;
    private ProgressDialog updatingDevicesProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        // Create a RealmConfiguration that saves the Realm file in the app's "files" directory.
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplicationContext()).build();
        Realm.setDefaultConfiguration(realmConfig);

        setContentView(R.layout.activity_phone);
        middleText = (TextView) findViewById(R.id.setup_message);
        setupButton = (Button) findViewById(R.id.smartthings_login_button);
        unpairButton = (Button) findViewById(R.id.smartthings_unpair_button);

        mainImageFrame = (FrameLayout) findViewById(R.id.main_image_frame);

        mainImage = (ImageView) findViewById(R.id.main_image);

        devicesListView = (ListView) findViewById(R.id.devices_list_view);

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSmartThingsLogin();
            }
        });

        unpairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAuthToken();
            }
        });

        // Get a Realm instance for this thread
        Realm realm = Realm.getDefaultInstance();

        //See if this activity was passed an endpoint URL by the pairing activity
        String endpointURL = getIntent().getStringExtra(ENDPOINT_URL);

        if (endpointURL!=null) {
            //Need to update devices and phrases
            updatingDevicesProgressDialog = new ProgressDialog(this);
            updatingDevicesProgressDialog.setMessage("Updating Devices and Phrases");
            updatingDevicesProgressDialog.setCancelable(false);
            updatingDevicesProgressDialog.setIndeterminate(true);
            updatingDevicesProgressDialog.show();
            updateModelAndPhrases(this);
            //show a stop message once its set...
        } else {
            //Check if user previously set devices or not
            //See if the user already set devices (and update the UI if they did)
            RealmResults<Device> devices = realm.where(Device.class).findAll();
            setUIBasedOnDeviceCount(devices);
        }


//        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.endpointURIKey, url);
//
//        if (getIntent().getBooleanExtra(ENDPOINT_URL,false)) {
//            updateModelAndPhrases();
//        } else {
//
//        }
//
//        //Set Devices Listener
//        RealmResults<Device> devices = realm.where(Device.class).findAll();
//
//        setUIBasedOnDeviceCount(devices);
//
//        devices.addChangeListener(new RealmChangeListener<RealmResults<Device>>() {
//            @Override
//            public void onChange(RealmResults<Device> devices) {
//                setUIBasedOnDeviceCount(devices);
//            }
//        });
    }

    //Change the UI based on how many devices the user set up
    private void setUIBasedOnDeviceCount(RealmResults<Device> devices) {
        if (devices.size()>0) {
            setSetupView();
        } else {
            setNotSetupView();
        }
    }

    //Called if devices are found
    private void setSetupView() {
        //Setup Listview with devices
        Realm realm = Realm.getDefaultInstance();
        final RealmResults<Device> devices = realm.where(Device.class).findAll();
        List<String> deviceNames = new LinkedList<>();
        for (Device device : devices) {
            deviceNames.add(device.getLabel());
        }

        final DevicesAdapter devicesAdapter = new DevicesAdapter(getBaseContext(),R.layout.simple_list_item_default,devices);

        //Header
        TextView pairedDevicesHeaderTextView = (TextView) findViewById(R.id.paired_devices_header);
        pairedDevicesHeaderTextView.setVisibility(View.VISIBLE);

        //Footer
        TextView pairedDevicesFooterTextView = (TextView) findViewById(R.id.paired_devices_footer);
        pairedDevicesFooterTextView.setVisibility(View.VISIBLE);

        devicesListView.setAdapter(devicesAdapter);

        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get the device that the user selected
                final Device device = devicesAdapter.getItem(position);
                //If item does not have a name
                    //Let the user enter a new name
                    final Subscriber<String> deviceLabelSubscriber = new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            throw new RuntimeException(e);
                        }

                        @Override
                        public void onNext(final String label) {
                            // Get a Realm instance for this thread
                            Realm realm = Realm.getDefaultInstance();

                            //Update the device label to this new name
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    device.setLabel(label);
                                }
                            });

                            devicesAdapter.notifyDataSetChanged();

                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Set Device Label");

                    final EditText input = new EditText(context);

                    builder.setView(input);

                    builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newLabel = input.getText().toString();
                            deviceLabelSubscriber.onNext(newLabel);

                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Don't do anything
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    //Set some padding
                    alertDialog.setView(input,40,20,40,10);
                    alertDialog.show();

                }
        });

        devicesListView.setVisibility(View.VISIBLE);

        middleText.setVisibility(View.GONE);
        setupButton.setText(getString(R.string.repair_smartthings));
        unpairButton.setVisibility(View.VISIBLE);
        mainImage.setVisibility(View.GONE);
    }

    private void setNotSetupView() {
//        devicesListView.setVisibility(View.GONE);
    }

    private void restartApp() {
        Intent intent = new Intent(this,PhoneActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        try {
            WearSocket.getInstance().disconnect();
        } catch (RuntimeException runtimeException) {
            Timber.d(runtimeException.toString());
        }
        super.onDestroy();
    }

    private void setupWearSocket(final Context context) {
        //If this is a flavor which uses no watch, ignore this step
        WearSocket wearSocket = WearSocket.getInstance();
        wearSocket.setupAndConnect(context, Values.WEAR_CAPABILITY, new WearSocket.onErrorListener() {
            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Please make sure CleverObjects is installed on your SmartWatch before pairing CleverObjects");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok, close app", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        builder.create().show();
                    }
                });
            }
        });
    }

    private void updateModelAndPhrases(final Context context) {

        //Update Device Model
        SmartThings.getInstance().setup(context); //Need to set this up before you can do anything fancy
        SmartThings.getInstance().getDevices(new Subscriber<List<Device>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onNext(final List<Device> devices) {


                MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context,"d09bbd29f9af4459edcacbad0785c4c0");
                for (Device device : devices) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Label",device.getLabel());
                        jsonObject.put("Type",device.getType());
                        jsonObject.put("ID",device.getId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mixpanelAPI.track("Update Devices:", jsonObject);
                }

                // Get a Realm instance for this thread
                Realm realm = Realm.getDefaultInstance();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //Delete existing devices
                        RealmResults<Device> result = realm.where(Device.class).findAll();
                        result.deleteAllFromRealm();
                        //Save the devices to realm

                        realm.copyToRealm(devices);
                    }
                });

                updatePhrases();
            }
        });

    }

    private void updatePhrases() {
        //Update Phrases
        SmartThings.getInstance().getPhrases(new Subscriber<List<String>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onNext(final List<String> phrases) {

                //Save the phrases if there are any
                if (phrases.size()>0) {
                    Realm realm = Realm.getDefaultInstance();

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            //Delete existing phrases
                            RealmResults<Phrase> result = realm.where(Phrase.class).findAll();
                            result.deleteAllFromRealm();
                            //Save the phrases to realm
                            for (String phrase : phrases) {
                                Phrase realmPhrase = realm.createObject(Phrase.class);
                                realmPhrase.setName(phrase);
                            }
                        }
                    });
                }

                updatingDevicesProgressDialog.dismiss();
                Timber.d("Done update models and phrases");
                setSetupView();

//                realm.executeTransactionAsync(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        //Delete existing phrases
//                        RealmResults<Phrase> result = realm.where(Phrase.class).findAll();
//                        result.deleteAllFromRealm();
//                        //Save the phrases to realm
//                        for (String phrase : phrases) {
//                            Phrase realmPhrase = realm.createObject(Phrase.class);
//                            realmPhrase.setName(phrase);
//                        }
//                    }
//                }, new Realm.Transaction.OnSuccess() {
//                    @Override
//                    public void onSuccess() {
//                        //Show user that update is complete
//                        Timber.d("Done update models and phrases");
//                        setSetupView();
//
//                    }
//                }, new Realm.Transaction.OnError() {
//                    @Override
//                    public void onError(Throwable error) {
//                        throw new RuntimeException(error);
//                    }
//                });

            }
        });
    }

//    private void updatePhrases(final List<Device> devices) {
//        //Update Phrases
//        SmartThings.getInstance().getPhrases(new Subscriber<List<String>>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                throw new RuntimeException(e);
//            }
//
//            @Override
//            public void onNext(List<Phrase> phrases) {
//                ModelAndKeyStorage.getInstance().storePhrases(context, phrases);
//                try {
//                    JSONObject props = new JSONObject();
//                    for (String phrase: phrases) {
//                        props.put(phrase,true);
//                    }
//                    mixpanelAPI.track("User Updated Phrases:", props);
//                } catch (JSONException e) {
//                    Timber.e("Unable to record user phrases");
//                }
//
//                //Set container to empty first (to refresh)
//                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.MODEL_KEY, new SmartThingsDataContainer());
//                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.MODEL_KEY, new SmartThingsDataContainer(devices,phrases));
//            }
//        });
//    }

    private void launchSmartThingsLogin() {
        Intent smartThingsLoginIntent = new Intent(this,SmartthingsLoginActivity.class);
        startActivity(smartThingsLoginIntent);
    }


//    private boolean hasUserAlreadySetUpSmartThings() {
//        if (ModelAndKeyStorage.getInstance().getData(context,ModelAndKeyStorage.authTokenKey)==null) {
//            return false;
//        }
//        return true;
//    }

    /** Removes the auth token stored on the mobile app and sends a message to the wear app telling it to kill its tokens */
    private void deleteAuthToken() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Unpair CleverObjects");
        alertDialogBuilder.setMessage("Are you sure you want to unpair CleverObjects from SmartThings?");
        alertDialogBuilder.setPositiveButton("Unpair", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Remove auth token
                ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.authTokenKey, null);
                //Remove local devices
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //Delete existing devices
                        RealmResults<Device> result = realm.where(Device.class).findAll();
                        result.deleteAllFromRealm();

                        //Delete existing Phrases
                        RealmResults<Phrase> phrasesResult = realm.where(Phrase.class).findAll();
                        phrasesResult.deleteAllFromRealm();
                    }
                });
                //Delete the stuff on the watch and restart the app
                if (!BuildConfig.FLAVOR.contains("noWatch")) {
                    WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH, Values.DELETE_KEY);
                }
                restartApp();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        alertDialogBuilder.create().show();
    }
}

