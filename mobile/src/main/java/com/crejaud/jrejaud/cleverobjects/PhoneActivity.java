package com.crejaud.jrejaud.cleverobjects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crejaud.jrejaud.cleverobjects.Server.SmartThings;
import com.github.jrejaud.WearSocket;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.Phrase;
import com.github.jrejaud.models.SmartThingsDataContainer;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.values.Values;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String UPDATE_WEAR_APP = "UPDATE_WEAR_APP";
    private boolean updatedThisSession = false;
    private ImageView mainImage;
    private MixpanelAPI mixpanelAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        //Init Mixpanel
        mixpanelAPI = MixpanelAPI.getInstance(this,"d09bbd29f9af4459edcacbad0785c4c0");

        // Create a RealmConfiguration that saves the Realm file in the app's "files" directory.
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).build();
        Realm.setDefaultConfiguration(realmConfig);

        setContentView(R.layout.activity_phone);
        middleText = (TextView) findViewById(R.id.setup_message);
        setupButton = (Button) findViewById(R.id.smartthings_login_button);
        unpairButton = (Button) findViewById(R.id.smartthings_unpair_button);

        mainImageFrame = (FrameLayout) findViewById(R.id.main_image_frame);

        mainImage = (ImageView) findViewById(R.id.main_image);

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

        //Set Devices Listener
        RealmResults<Device> devices = realm.where(Device.class).findAll();
        devices.addChangeListener(new RealmChangeListener<RealmResults<Device>>() {
            @Override
            public void onChange(RealmResults<Device> devices) {
                if (devices.size()>0) {
                    setSetupView();
                } else {
                    setNotSetupView();
                }

            }
        });
    }

    private void setSetupView() {
        middleText.setText(getString(R.string.paired_message));
        setupButton.setText(getString(R.string.repair_smartthings));
        unpairButton.setVisibility(View.VISIBLE);
        mainImage.setImageResource(R.drawable.ic_app_ready);
    }

    private void setNotSetupView() {
        mainImage.setImageResource(R.drawable.smartwatch_home);
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

        if (getIntent().getBooleanExtra(UPDATE_WEAR_APP, false)) {
            if (!updatedThisSession) {
                updateModelAndPhrases(this);
                updatedThisSession = true;
            }
        }
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
                // Get a Realm instance for this thread
                Realm realm = Realm.getDefaultInstance();

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //Delete existing devices
                        RealmResults<Device> result = realm.where(Device.class).findAll();
                        result.deleteAllFromRealm();
                        //Save the devices to realm
                        realm.copyToRealm(devices);

                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        //Success saved devices
                        //Update phrases next
                        updatePhrases();

                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        throw new RuntimeException(error);
                    }
                });
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
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransactionAsync(new Realm.Transaction() {
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
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        //Show user that update is complete
                        setSetupView();

                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        throw new RuntimeException(error);
                    }
                });

            }
        });
    }

    @Deprecated
    private void updatePhrases(final List<Device> devices) {
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
            public void onNext(List<String> phrases) {
                ModelAndKeyStorage.getInstance().storePhrases(context, phrases);
                try {
                    JSONObject props = new JSONObject();
                    for (String phrase: phrases) {
                        props.put(phrase,true);
                    }
                    mixpanelAPI.track("User Updated Phrases:", props);
                } catch (JSONException e) {
                    Timber.e("Unable to record user phrases");
                }

                //Set container to empty first (to refresh)
                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.MODEL_KEY, new SmartThingsDataContainer());
                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.MODEL_KEY, new SmartThingsDataContainer(devices,phrases));
            }
        });
    }

    private void launchSmartThingsLogin() {
        Intent smartThingsLoginIntent = new Intent(this,SmartthingsLoginActivity.class);
        startActivity(smartThingsLoginIntent);
    }

    @Deprecated
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
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //Delete existing devices
                        RealmResults<Device> result = realm.where(Device.class).findAll();
                        result.deleteAllFromRealm();

                        //Delete existing Phrases
                        RealmResults<Phrase> phrasesResult = realm.where(Phrase.class).findAll();
                        phrasesResult.deleteAllFromRealm();

                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        //Delete the stuff on the watch and restart the app
                        if (BuildConfig.FLAVOR.contains("noWatch")) {
                            return;
                        }
                        WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH, Values.DELETE_KEY);
                        restartApp();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        throw new RuntimeException(error);
                    }
                });
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

