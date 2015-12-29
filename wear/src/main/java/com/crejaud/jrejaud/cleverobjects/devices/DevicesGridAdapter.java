package com.crejaud.jrejaud.cleverobjects.devices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.wearable.view.GridPagerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crejaud.jrejaud.cleverobjects.PhrasesActivity;
import com.crejaud.jrejaud.cleverobjects.R;
import com.github.jrejaud.models.Device;
import com.github.jrejaud.models.SmartThingsModelManager;

import java.util.List;
import java.util.Objects;

/**
 * Created by jrejaud on 7/5/15.
 */
public class DevicesGridAdapter extends GridPagerAdapter {

    private Context context;
    private List<Device> devices;
    public static final int SPEECH_REQUEST_CODE = 0;

    public DevicesGridAdapter(Context context, List<Device> devices) {
        this.context =context;
        this.devices = devices;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return devices.size()+2;
    }

    @Override
    protected Object instantiateItem(ViewGroup viewGroup, int row, final int col) {
        View view = View.inflate(context, R.layout.main_menu_button,null);
        TextView itemName = (TextView) view.findViewById(R.id.menu_picker_name);
        ImageView menuIcon = (ImageView) view.findViewById(R.id.menu_picker_icon);
        final ImageView menuCircle = (ImageView) view.findViewById(R.id.menu_picker_color_circle);
        if (col==0) {
            itemName.setText("Voice");
            //Use xl size and 24 dp for icons
            menuIcon.setImageResource(R.drawable.ic_mic_white_24dp);
        }
        else if (col==1) {
            itemName.setText("Hello, Home");
            menuIcon.setImageResource(R.drawable.ic_home_white_24dp);
        }
        else {
            //It is a device!
            Device device = devices.get(col-2);
            itemName.setText(device.getLabel());
            if (Objects.equals(device.getType(), Device.LOCK)) {
                menuIcon.setImageResource(R.drawable.ic_lock_white_24dp);
            } else if (Objects.equals(device.getType(), Device.SWITCH)) {
                menuIcon.setImageResource(R.drawable.ic_radio_button_checked_white_24dp);
            }
            //todo set icon to whatever the hell it is (lock/switch and on/off)
        }

        view.setTag(col);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (col == 0) {
                    startVoiceRecognition();
                } else if (col == 1) {
                    startPhrasesActivity();
                } else {
                    DeviceStateChange deviceStateChange = new DeviceStateChange();
                    deviceStateChange.updateDeviceState(col-2,Device.TOGGLE);
                }
            }
        });

        menuIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    menuCircle.setImageResource(R.drawable.wl_circle_pushed);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    menuCircle.setImageResource(R.drawable.wl_circle);
                }
                return false;
            }
        });

        viewGroup.addView(view);
        return col;

    }

    @Override
    protected void destroyItem(ViewGroup viewGroup, int i, int i1, Object o) {
        //wtf does this do??
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        //This wizardry worked before, it probably will again
        int tag = (Integer)view.getTag();
        int itemTag = (Integer) o;
        return itemTag == tag;
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        ((Activity)context).startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    private void startPhrasesActivity() {
        //Start Phrase List if there are any phrases
        if (SmartThingsModelManager.getInstance().getPhrases().size()==0) {
            Toast.makeText(context,"You need to set up some phrases on SmartThings first",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent phraseActivityIntent = new Intent(context, PhrasesActivity.class);
        context.startActivity(phraseActivityIntent);
    }
}
