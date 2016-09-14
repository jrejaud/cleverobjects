package com.crejaud.jrejaud.cleverobjects.Authentication;

/**
 * Created by jrejaud on 7/7/15.
 */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EndPointURL extends AsyncTask<Void,Void,String> {

    private Context context;
    private endpointInterface endpointINT;
    private String accessToken;

    @Override
    protected String doInBackground(Void...voids){
            return getEndpoints();
            }

    public interface endpointInterface {
        void foundEndPoint(String url);

    }

    public EndPointURL(Context context, String accessToken) {
        this.context = context;
        this.accessToken = accessToken;
        endpointINT = (endpointInterface) context;
    }

    @Override
    protected void onPostExecute(String url) {
        super.onPostExecute(url);
        endpointINT.foundEndPoint(url);
    }

    public String getEndpoints() {
        Uri endpointURI = getEndpointUri(accessToken);

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(String.valueOf(endpointURI));
        //Add headers
        request.addHeader("Authorization", "Bearer " + accessToken);


        try {
            HttpResponse response = httpclient.execute(request);

            String responseString = getStringfromResponse(response);
            //To do: Error check that string it not null

            //Remove brackets from Response String (in order to parse it as JSON)
            String fixedResponseString = responseString.substring(1, responseString.length() - 1);
            //Log.d(context.getString(R.string.TAG), "Fixed Response String: " + fixedResponseString);

            JSONObject obj = new JSONObject(fixedResponseString);

//            String endpointURL = obj.get("url").toString();
//
//            //Remove first / from endpointURL
//            endpointURL = endpointURL.substring(29, endpointURL.length());

            String fullEndpointURL = obj.getString("uri");



            //Get the values from the Response URL!!!! (for switches)
//            Uri endpointValues = getEndpointValuesUri(endpointURL, "switches");
//
//            Log.d(context.getString(R.string.TAG), "Endpoint URL: " + endpointValues.toString()); //Need to keep this thing!
//            HttpGet requestValues = new HttpGet(String.valueOf(endpointValues));
//
//            //Add headers
//            requestValues.addHeader("Content-Type","application/json");
//            requestValues.addHeader("Authorization","Bearer "+accessToken);
//
//            HttpResponse responseValues = httpclient.execute(requestValues);
//            String reponseValuesString = getStringfromResponse(responseValues);


            return fullEndpointURL; //endpointValues.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Get the URL values of the endpoint items (switches and stuff like that)
    private String getStringfromResponse(HttpResponse response) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            response.getEntity().writeTo(out);
            String responseString = out.toString();
            return responseString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Deprecated
    private Uri getEndpointValuesUri(String endpointsURL, String itemType) {
        Uri.Builder builder = new Uri.Builder();
        return builder.scheme("https")
                .authority("graph.api.smartthings.com")
                .appendPath("api")
                .appendPath("smartapps")
                .appendPath("installations")
                .appendPath(endpointsURL)
                .appendPath(itemType).build();
    }

    private Uri getEndpointUri(String accessToken) {
        Uri.Builder builder = new Uri.Builder();
        return builder.scheme("https")
                .authority("graph.api.smartthings.com")
                .appendPath("api")
                .appendPath("smartapps")
                .appendPath("endpoints").build();
        //.appendPath(context.getString(R.string.client_id))
        //.appendQueryParameter("access_token",accessToken).build();
    }
}