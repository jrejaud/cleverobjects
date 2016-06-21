package com.crejaud.jrejaud.cleverobjects;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.crejaud.jrejaud.cleverobjects.Authentication.AccessCode;
import com.crejaud.jrejaud.cleverobjects.Authentication.AccessToken;
import com.crejaud.jrejaud.cleverobjects.Authentication.EndPointURL;
import com.github.jrejaud.storage.ModelAndKeyStorage;

import timber.log.Timber;


public class SmartthingsLoginActivity extends CleverObjectsActivity implements AccessCode.accessInterface, AccessToken.AccessTokenInterface, EndPointURL.endpointInterface, AccessCode.WebsiteLoadedInterface {

    private Context context;
    private WebView loginWebView;
    private ProgressDialog websiteLoadingDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartthings_login);
        context = this;
        loginWebView = (WebView) findViewById(R.id.smartthings_login_webview);
        websiteLoadingDialog = new ProgressDialog(context);
        websiteLoadingDialog.setCancelable(false);
        websiteLoadingDialog.setMessage("Loading SmartThings website");
        websiteLoadingDialog.setIndeterminate(true);
        websiteLoadingDialog.show();
        startLoginProcess();
    }

    @Override
    public void websiteLoaded() {
        if (websiteLoadingDialog.isShowing()) {
            websiteLoadingDialog.hide();
        }
    }

    private void startLoginProcess() {
        Timber.d("Starting login process");

        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.clientIDKey,"9a771704-177f-440d-b771-f7f73d64d925");
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.clientSecretKey,"cc7d909f-41eb-4081-9cd0-663d671acc77");
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
        restartApp();
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
        alertDialogBuilder.setMessage("Successfully paired CleverObjects");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder.create().show();
    }

    private void restartApp() {
        Intent intent = new Intent(this,PhoneActivity.class);
        intent.putExtra(PhoneActivity.UPDATE_WEAR_APP,true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
