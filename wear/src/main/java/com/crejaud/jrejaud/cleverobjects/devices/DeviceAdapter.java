package com.crejaud.jrejaud.cleverobjects.devices;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crejaud.jrejaud.cleverobjects.R;
import com.github.jrejaud.models.Device;

import java.util.List;

/**
 * Created by jrejaud on 6/29/15.
 */
public class DeviceAdapter extends WearableListView.Adapter {
        private List<Device> devices;
        private final Context mContext;
        private final LayoutInflater mInflater;

        // Provide a suitable constructor (depends on the kind of dataset)
        public DeviceAdapter(Context context, List<Device> devices) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        // Provide a reference to the type of views you're using
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView textView;
            private ImageView circleIcon;
            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.name);
                //circleIcon = (ImageView) itemView.findViewById(R.id.circle);
            }
        }

        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            // Inflate our custom layout for list items
            return new ItemViewHolder(mInflater.inflate(R.layout.phrases_list_item, null));
        }

        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {
            // retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;
            ImageView circleIcon = itemHolder.circleIcon;
            Log.d("TAG","position: "+position);
            // replace text contents
            if (position==0) {
                view.setText("Voice");
                //circleIcon.setVisibility(View.INVISIBLE);
            }
            else if (position==1) {
                view.setText("Hello Home");
                //circleIcon.setVisibility(View.INVISIBLE);
            }
            else if (position>=2){
                Device device = devices.get(position-2);
                view.setText(device.getLabel());
                if (device.getValue()==null) {
                    //circleIcon.setVisibility(View.INVISIBLE);
                }
                else if (!device.getValue().equals(Device.ON)) {
                    //circleIcon.setVisibility(View.INVISIBLE);
                }
            }
            // replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return devices.size() + 2;
        }

}
