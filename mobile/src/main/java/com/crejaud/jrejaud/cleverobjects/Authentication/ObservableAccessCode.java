package com.crejaud.jrejaud.cleverobjects.Authentication;

import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by jrejaud on 8/3/15.
 */
public class ObservableAccessCode {

    private Context context;
    private final String codeURLStart = "https://graph.api.smartthings.com/oauth/callback?code=";

    public Observable<String> getAccessCode(Context context, final WebView webView) {
        this.context = context;
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String accessCodeURI = getAccessCodeUri();
                loadWebView(accessCodeURI, webView, subscriber);
                subscriber.onNext("A test access Code!");
            }
        });
    }

    private String getAccessCodeUri() {
        return "https://graph.api.smartthings.com/oauth/authorize?response_type=code&client_id="+ com.github.jrejaud.storage.ModelAndKeyStorage.getInstance().getData(context, com.github.jrejaud.storage.ModelAndKeyStorage.clientIDKey)+"&scope=app&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback";
    }

    private void loadWebView(String accessURI, WebView logInWebView, final Subscriber subscriber) {
        WebSettings webSettings = logInWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        logInWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url.startsWith(codeURLStart)) {
                    //Toast.makeText(context,"URL: "+url,Toast.LENGTH_SHORT);
                    int startLoc = url.indexOf("code=") + 5;
                    String accessCode = url.substring(startLoc, url.length());
                    subscriber.onNext(accessCode);
                }
            }
        });
        logInWebView.loadUrl(accessURI);
    }


}
