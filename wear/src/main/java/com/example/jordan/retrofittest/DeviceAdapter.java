package com.example.jordan.retrofittest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.example.jordan.retrofittest.Models.Device;

/**
 * Created by Jordan on 6/14/2015.
 */
public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<Device> devices;

    public DeviceAdapter(Context context, List<Device> devices) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.devices = devices;
    }

    @Override
    public int getCount() {
        return devices.size()+2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            convertView = layoutInflater.inflate(R.layout.grid_device,null);
        }

        TextView deviceLabel = (TextView) convertView.findViewById(R.id.grid_device_label);

        if (position == 0) {
            deviceLabel.setText("Voice");
        }
        else if (position == 1) {
            deviceLabel.setText("Phrases");
        }
        else {
            deviceLabel.setText(devices.get(position-2).getLabel());
            //TODO show if device is on or off
        }

        return convertView;
    }

}
