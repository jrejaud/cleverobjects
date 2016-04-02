package com.crejaud.jrejaud.cleverobjects;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.crejaud.jrejaud.cleverobjects.Server.SmartThings;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.Values;
import com.github.jrejaud.wear_socket.WearSocket;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Subscriber;
import timber.log.Timber;


public class PhoneActivity extends CleverObjectsActivity {

    private Context context;
    private TextView middleText;
    private Button setupButton;
    private Button unpairButton;
    private FrameLayout mainImage;
    public static final String UPDATE_WEAR_APP = "UPDATE_WEAR_APP";
    private boolean updatedThisSession = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        setContentView(R.layout.activity_phone);
        middleText = (TextView) findViewById(R.id.setup_message);
        setupButton = (Button) findViewById(R.id.smartthings_login_button);
        unpairButton = (Button) findViewById(R.id.smartthings_unpair_button);

        mainImage = (FrameLayout) findViewById(R.id.main_image);

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

        if (hasUserAlreadySetUpSmartThings()) {
            middleText.setText(getString(R.string.paired_message));
            setupButton.setText(getString(R.string.repair_smartthings));
            unpairButton.setVisibility(View.VISIBLE);
            mainImage.setVisibility(View.VISIBLE);
        }
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


        if (getIntent().getBooleanExtra(UPDATE_WEAR_APP, false) && hasUserAlreadySetUpSmartThings()) {
            if (!updatedThisSession) {
                setupWearSocket(this);
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
        if (BuildConfig.FLAVOR.contains("noWatch")) {
            return;
        }
        WearSocket wearSocket = WearSocket.getInstance();
        wearSocket.setupAndConnect(context, Values.WEAR_CAPABILITY, new WearSocket.onErrorListener() {
            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("You need to have an Android Wear Device paired to your phone to use CleverObjects");
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
            public void onNext(List<Device> devices) {
                ModelAndKeyStorage.getInstance().storeDevices(context, devices);
                if (BuildConfig.FLAVOR.contains("noWatch")) {
                    return;
                }
                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.MODEL_KEY, devices);
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
            public void onNext(List<String> phrases) {
                ModelAndKeyStorage.getInstance().storePhrases(context, phrases);
                if (BuildConfig.FLAVOR.contains("noWatch")) {
                    return;
                }
                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.PHRASES_KEY, phrases);
            }
        });
    }

    private void launchSmartThingsLogin() {
        Intent smartThingsLoginIntent = new Intent(this,SmartthingsLoginActivity.class);
        startActivity(smartThingsLoginIntent);
    }

    private boolean hasUserAlreadySetUpSmartThings() {
        if (ModelAndKeyStorage.getInstance().getData(context,ModelAndKeyStorage.authTokenKey)==null) {
            return false;
        }
        return true;
    }

    /** Removes the auth token stored on the mobile app and sends a message to the wear app telling it to kill its tokens */
    private void deleteAuthToken() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Unpair CleverObjects");
        alertDialogBuilder.setMessage("Are you sure you want to unpair CleverObjects from SmartThings?");
        alertDialogBuilder.setPositiveButton("Unpair", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.authTokenKey, null);
                if (BuildConfig.FLAVOR.contains("noWatch")) {
                    return;
                }
                WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH, Values.DELETE_KEY);
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

