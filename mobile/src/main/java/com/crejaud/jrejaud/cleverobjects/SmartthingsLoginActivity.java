package com.crejaud.jrejaud.cleverobjects;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.crejaud.jrejaud.cleverobjects.Authentication.AccessCode;
import com.crejaud.jrejaud.cleverobjects.Authentication.AccessToken;
import com.crejaud.jrejaud.cleverobjects.Authentication.EndPointURL;
import com.github.jrejaud.storage.ModelAndKeyStorage;

import timber.log.Timber;


public class SmartthingsLoginActivity extends CleverObjectsActivity implements AccessCode.accessInterface, AccessToken.AccessTokenInterface, EndPointURL.endpointInterface {

    private Context context;
    private WebView loginWebView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartthings_login);
        context = this;
        loginWebView = (WebView) findViewById(R.id.smartthings_login_webview);
        startLoginProcess();
    }

    private void startLoginProcess() {
        Timber.d("Starting login process");
        //Old client ID "3439c1e4-73a6-4db3-a78c-af2098f585fd"
        //Old client secret "8633a1ae-98ad-4c0f-8f44-0faccfa13cf4"
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.clientIDKey,"162a3536-b3e8-4bb7-a5df-212a9187ba30");
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.clientSecretKey,"c5e9c060-5259-4b2b-ac63-48fa465bb74b");

        new AccessCode(this,loginWebView).getAccessCode();
    }

    @Override
    public void foundAccessCode(String accessCode) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Setting up CleverObjects");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Timber.d("Found access code!");
        loginWebView.setVisibility(View.INVISIBLE);
        new AccessToken(context,accessCode).execute();
    }

    @Override
    public void foundAccessToken(String accessToken) {
        if (accessToken==null) {
            progressDialog.hide();
            showErrorMessage();
            return;
        }
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.authTokenKey, accessToken);
        new EndPointURL(context,accessToken).execute();
    }

    @Override
    public void foundEndPoint(String url) {
        if (url==null) {
            progressDialog.hide();
            showErrorMessage();
            return;
        }
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.endpointURIKey, url);
        progressDialog.hide();
        showCompletedAlertDialog();
    }

    private void showErrorMessage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Error");
        alertDialogBuilder.setMessage("Error setting up CleverObjects, please try again");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder.create().show();
    }

    private void showCompletedAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Success");
        alertDialogBuilder.setMessage("Successfully set up CleverObjects, load the app up on your watch and try it out.");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder.create().show();
    }
}
