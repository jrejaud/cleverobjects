package com.crejaud.jrejaud.cleverobjects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jrejaud.models.Device;

import java.util.List;

import timber.log.Timber;

/**
 * Created by jrejaud on 8/8/16.
 */
public class DevicesAdapter extends ArrayAdapter<Device> {

    private List<Device> devices;

    public DevicesAdapter(Context context, int resource, List<Device> objects) {
        super(context, resource, objects);
        devices = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Device device = devices.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_list_item_default, parent, false);
        }
        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.text);
        ImageView deviceIcon = (ImageView) convertView.findViewById(R.id.image);
        // Populate the data into the template view using the data object
        title.setText(device.getLabel());
        Timber.d("Device found: "+device.getLabel()+", "+device.getType());
        if (device.getType().equals("lock")) {
            deviceIcon.setImageResource(R.drawable.ic_lock_outline_black_24dp);
        } else if (device.getType().equals("switch")) {
            deviceIcon.setImageResource(R.drawable.ic_radio_button_checked_black_24dp);
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
