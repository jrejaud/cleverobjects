package com.crejaud.jrejaud.cleverobjects.Authentication;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by jrejaud on 8/2/15.
 */
public class NewAuthentication {

    private final static String TAG = NewAuthentication.class.getSimpleName();
    private String accessCode;
    private Context context;

    public NewAuthentication(Context context) {
        this.context = context;
    }

    public void setup(WebView webView) {
        ObservableAccessCode observableAccessCode = new ObservableAccessCode();
        observableAccessCode.getAccessCode(context,webView)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(getAccessToken);



        //create observable that provides the accessCode
        //Use the access code to get the accessToken (and save it)
        //Use the access token to get the endpoint URL (and save it)
        // Then you are ready
    }

    Func1<String, String> getAccessToken = new Func1<String, String>() {
        @Override
        public String call(String accessCode) {
            Log.d(TAG,"Provided access code: "+accessCode);
            return null;
        }
    };

    private void getAccessCode() {

    }

    private void getAccessToken(String accessCode) {

    }

    private void getEndpointURL() {

    }


}
