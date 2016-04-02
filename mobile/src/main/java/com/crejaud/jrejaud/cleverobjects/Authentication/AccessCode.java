package com.crejaud.jrejaud.cleverobjects.Authentication;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.jrejaud.storage.ModelAndKeyStorage;

/**
 * Created by jrejaud on 7/7/15.
 */
public class AccessCode {

    private Context context;
    private WebView logInWebView;
    public accessInterface accessINT;
    private WebsiteLoadedInterface websiteLoadedInterface;

    public interface accessInterface {
        void foundAccessCode(String accessCode);
    }

    public AccessCode(Context context, WebView logInWebView) {
        this.context = context;
        this.websiteLoadedInterface = (WebsiteLoadedInterface) context;
        this.logInWebView = logInWebView;
        clearWebViewHistory();
        accessINT = (accessInterface) context;
    }

    private void clearWebViewHistory() {
        //Forget if user is already logged into SB (not sure which of these lines does it haha)
        this.logInWebView.clearFormData();
        this.logInWebView.getSettings().setSaveFormData(false);
        this.logInWebView.getSettings().setSavePassword(false);
        android.webkit.CookieManager.getInstance().removeAllCookie();
    }

    public void getAccessCode() {
        Uri accessCodeUri = getNewAccessCodeUri();
        loadWebView(accessCodeUri);
    }

    private Uri getAccessCodeUri() {
        String updateUriv3 = "https://graph.api.smartthings.com/oauth/confirm_access?response_type=code&scope=app&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback&client_id=3439c1e4-73a6-4db3-a78c-af2098f585fd";
        Uri.Builder builder = new Uri.Builder();
        String oldRedirectURI = "https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback";
        String newRedirectURI = "%2Fcallback";
        return builder.scheme("https")
                .authority("graph.api.smartthings.com")
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", ModelAndKeyStorage.getInstance().getData(context,ModelAndKeyStorage.clientIDKey))
                .appendQueryParameter("scope", "app")
                .appendQueryParameter("redirect_uri", oldRedirectURI).build();
    }

    private Uri getNewAccessCodeUri() {
        //String updateUriv3 = "https://graph.api.smartthings.com/oauth/confirm_access?response_type=code&scope=app&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback&client_id=3439c1e4-73a6-4db3-a78c-af2098f585fd";
//        Uri.Builder builder = new Uri.Builder();
//        String redirectURI = "https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback";
//        return builder.scheme("https")
//                .authority("graph.api.smartthings.com")
//                .appendPath("oauth")
//                .appendPath("confirm_access")
//                .appendQueryParameter("response_type", "code")
//                .appendQueryParameter("scope", "app")
//                .appendQueryParameter("redirect_uri", redirectURI)
//                .appendQueryParameter("client_id",ModelAndKeyStorage.getInstance().getData(context,ModelAndKeyStorage.clientIDKey)).build();
        String url = "https://graph.api.smartthings.com/oauth/authorize?response_type=code&client_id="+ModelAndKeyStorage.getInstance().getData(context,ModelAndKeyStorage.clientIDKey)+"&scope=app&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback";
        return Uri.parse(url);
    }

    private void loadWebView(Uri accessURI) {
        String accessURL = accessURI.toString();
        final String codeURLStart = "https://graph.api.smartthings.com/oauth/callback?code=";
        WebSettings webSettings = logInWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        logInWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                websiteLoadedInterface.websiteLoaded();
                super.onPageStarted(view, url, favicon);
                if (url.startsWith(codeURLStart)) {
                    //Toast.makeText(context,"URL: "+url,Toast.LENGTH_SHORT);
                    int startLoc = url.indexOf("code=") + 5;
                    String accessCode = url.substring(startLoc, url.length());
                    //Have the accessCode, get the accessToken
                    //Call back to main activity with new code (and orders to move to next activity)
                    accessINT.foundAccessCode(accessCode);
                }
            }
        });
        //https://graph.api.smartthings.com/oauth/confirm_access?response_type=code&scope=app&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback&client_id=3439c1e4-73a6-4db3-a78c-af2098f585fd
        logInWebView.loadUrl(accessURL);
    }

    public interface WebsiteLoadedInterface {
        void websiteLoaded();
    }

}
