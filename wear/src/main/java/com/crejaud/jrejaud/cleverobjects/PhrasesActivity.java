package com.crejaud.jrejaud.cleverobjects;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import com.crejaud.jrejaud.cleverobjects.phrases.PhrasesAdapter;
import com.github.jrejaud.models.SmartThingsModelManager;
import com.github.jrejaud.values.DeviceStateChangeMessage;
import com.github.jrejaud.values.Values;
import com.github.jrejaud.wear_socket.WearSocket;

import org.json.JSONException;
import org.json.JSONObject;

public class PhrasesActivity extends Activity {

    private WearableListView phrasesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phrases_list);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                phrasesListView = (WearableListView) findViewById(R.id.phrasesListView);
                phrasesListView.setAdapter(new PhrasesAdapter(context, SmartThingsModelManager.getPhrases()));
                phrasesListView.setClickListener(new WearableListView.ClickListener() {
                    @Override
                    public void onClick(WearableListView.ViewHolder viewHolder) {
                        Log.d(Values.TAG,"pos: "+viewHolder.getPosition());
                        PhrasesAdapter.PhraseViewHolder itemHolder = (PhrasesAdapter.PhraseViewHolder) viewHolder;
                        String phrase = itemHolder.getPhrase();
                        Log.d(Values.TAG, "User select phrase: " + phrase);
                        JSONObject message = new JSONObject();
                        try {
                            message.put(DeviceStateChangeMessage.TYPE, DeviceStateChangeMessage.PHRASE);
                            message.put(DeviceStateChangeMessage.DATA,phrase);
                            WearSocket.getInstance().sendMessage(Values.MESSAGE_PATH, message.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }

                    @Override
                    public void onTopEmptyRegionClick() {
                        //WTF is the point of this??
                    }
                });
            }
        });
    }
}
