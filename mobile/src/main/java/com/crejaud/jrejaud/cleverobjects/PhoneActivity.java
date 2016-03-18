package com.crejaud.jrejaud.cleverobjects;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crejaud.jrejaud.cleverobjects.Server.SmartThings;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.Values;
import com.github.jrejaud.wear_socket.WearSocket;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;


public class PhoneActivity extends CleverObjectsActivity {

    private Context context;
    private TextView middleText;
    private Button setupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        setContentView(R.layout.activity_phone);
        middleText = (TextView) findViewById(R.id.setup_message);
        setupButton = (Button) findViewById(R.id.smartthings_login_button);

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSmartThingsLogin();
            }
        });

        if (hasUserAlreadySetUpSmartThings()) {
            middleText.setText("SmartThings is paired and CleverObjects is ready on your watch");
            setupWearSocket(this);
            updateModelAndPhrases(this);
        }

    }

    //
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (hasUserAlreadySetUpSmartThings()) {
//            middleText.setText("SmartThings is paired and CleverObjects is ready on your watch");
//            setupWearSocket();
//            updateModelAndPhrases();
//        }
//    }

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
        WearSocket wearSocket = WearSocket.getInstance();
        wearSocket.setupAndConnect(context, Values.WEAR_CAPABILITY, new WearSocket.onErrorListener() {
            @Override
            public void onError(Throwable throwable) {
                new Handler().post(new Runnable() {
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
        SmartThings.getInstance().getDevices(new Callback() {
            @Override
            public void success(Object o, Response response) {
                ModelAndKeyStorage.getInstance().storeDevices(context,SmartThingsModelManager.getInstance().getDevices());
                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.MODEL_KEY, SmartThingsModelManager.getInstance().getDevices());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        //Update Phrases
        SmartThings.getInstance().getPhrases(new Callback() {
            @Override
            public void success(Object o, Response response) {
                ModelAndKeyStorage.getInstance().storePhrases(context, SmartThingsModelManager.getInstance().getPhrases());
                WearSocket.getInstance().updateDataItem(Values.DATA_PATH, Values.PHRASES_KEY, SmartThingsModelManager.getInstance().getPhrases());
            }

            @Override
            public void failure(RetrofitError error) {

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
}

