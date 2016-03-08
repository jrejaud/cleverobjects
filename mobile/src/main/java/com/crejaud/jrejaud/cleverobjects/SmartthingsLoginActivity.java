package com.crejaud.jrejaud.cleverobjects;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartthings_login);
        context = this;
        loginWebView = (WebView) findViewById(R.id.smartthings_login_webview);
        startLoginProcess();
    }

    private void startLoginProcess() {
        //todo need to find a better way to obs this
        Timber.d("Starting login process");
        //Old client ID "3439c1e4-73a6-4db3-a78c-af2098f585fd"
        //Old client secret "8633a1ae-98ad-4c0f-8f44-0faccfa13cf4"
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.clientIDKey,"6359b0db-6481-44b8-b68f-68012818fa7a");
        ModelAndKeyStorage.getInstance().storeData(context,ModelAndKeyStorage.clientSecretKey,"601f1221-28ec-451a-92e2-3b87fec0a2bc");

        new AccessCode(this,loginWebView).getAccessCode();
    }

    @Override
    public void foundAccessCode(String accessCode) {
        Timber.d("Found access code!");
        Toast.makeText(context,"Loading...",Toast.LENGTH_SHORT).show();
        loginWebView.setVisibility(View.INVISIBLE);
        new AccessToken(context,accessCode).execute();
    }

    @Override
    public void foundAccessToken(String accessToken) {
        if (accessToken==null) {
            //todo connection error
            Toast.makeText(context,"Connection error :(",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.authTokenKey, accessToken);
        new EndPointURL(context,accessToken).execute();
    }

    @Override
    public void foundEndPoint(String url) {
        if (url==null) {
            //todo connection error
            Toast.makeText(context,"Connection error :(",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ModelAndKeyStorage.getInstance().storeData(context, ModelAndKeyStorage.endpointURIKey, url);
        finish();
    }
}
