package com.crejaud.jrejaud.cleverobjects.Authentication;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;


import com.github.jrejaud.storage.ModelAndKeyStorage;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jrejaud on 7/7/15.
 */
public class AccessToken extends AsyncTask<Void,Void,String> {

    private Context context;
    private String accessCode;
    AccessTokenInterface accessTokenInterface;

    public interface AccessTokenInterface {
        void foundAccessToken(String accessToken);
    }

    public AccessToken(Context context, String accessCode) {
        this.context = context;
        this.accessCode = accessCode;
        accessTokenInterface = (AccessTokenInterface) context;

    }

    @Override
    protected String doInBackground(Void... voids) {

        return getAccessToken();
    }

    public String getAccessToken() {

        String accessToken = null;
        Uri AccessTokenURI = getAccessTokenUri(accessCode, ModelAndKeyStorage.getInstance().getData(context,ModelAndKeyStorage.clientIDKey),ModelAndKeyStorage.getInstance().getData(context, ModelAndKeyStorage.clientSecretKey));

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(String.valueOf(AccessTokenURI));

        //This is no longer working for now, injecting it hard into the URL
//        request.addHeader(BasicScheme.authenticate(
//                new UsernamePasswordCredentials(ModelAndKeyStorage.getInstance().getData(context, ModelAndKeyStorage.clientIDKey), ModelAndKeyStorage.getInstance().getData(context, ModelAndKeyStorage.clientSecretKey)),
//                "UTF-8", false));

        //some hackz!
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            HttpResponse response = httpclient.execute(request);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            String responseString = out.toString();

            JSONObject obj = new JSONObject(responseString);
            accessToken = obj.get("access_token").toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return accessToken;
    }


    private Uri getAccessTokenUri(String accessCode, String clientID, String clientSecret) {
//        String url = "https://graph.api.smartthings.com/oauth/token?code="+accessCode+"&grant_type=authorization_code&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback&scope=app";
        String workingURL = "https://graph.api.smartthings.com/oauth/token?grant_type=authorization_code&client_id="+clientID+"&client_secret="+clientSecret+"&code="+accessCode+"&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback&scope=app";
        return Uri.parse(workingURL);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        accessTokenInterface.foundAccessToken(s);
    }
}
