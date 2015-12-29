package com.crejaud.jrejaud.cleverobjects;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crejaud.jrejaud.cleverobjects.Server.SmartThings;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.storage.ModelAndKeyStorage;
import com.github.jrejaud.values.Values;
import com.github.jrejaud.wear_socket.WearSocket;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PhoneActivity extends ActionBarActivity {

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
        WearSocket.getInstance().disconnect();
        super.onDestroy();
    }

    private void setupWearSocket(Context context) {
        WearSocket wearSocket = WearSocket.getInstance();
        wearSocket.setupAndConnect(context,Values.WEAR_CAPABILITY);
    }

    private void updateModelAndPhrases(Context context) {
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
        Intent smartThingsLoginIntent = new Intent(this,SmartthingsLogin.class);
        startActivity(smartThingsLoginIntent);
    }

    private boolean hasUserAlreadySetUpSmartThings() {
        if (ModelAndKeyStorage.getInstance().getData(context,ModelAndKeyStorage.authTokenKey)==null) {
            return false;
        }
        return true;
    }
}

