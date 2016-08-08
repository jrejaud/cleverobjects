package com.crejaud.jrejaud.cleverobjects.phrases;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crejaud.jrejaud.cleverobjects.R;
import com.github.jrejaud.models.Phrase;
import com.github.jrejaud.models.PhrasePOJO;

import java.util.List;

/**
 * Created by jrejaud on 6/29/15.
 */
public class PhrasesAdapter extends WearableListView.Adapter {
        private List<PhrasePOJO> phrases;
        private final Context mContext;
        private final LayoutInflater mInflater;

        // Provide a suitable constructor (depends on the kind of phrases)
        public PhrasesAdapter(Context context, List<PhrasePOJO> phrases) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            this.phrases = phrases;
        }

        // Provide a reference to the type of views you're using
        public static class PhraseViewHolder extends WearableListView.ViewHolder {
            private TextView textView;
            public PhraseViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.name);
            }
            public String getPhrase() {
                return textView.getText().toString();
            }

        }

        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            // Inflate our custom layout for list items
            return new PhraseViewHolder(mInflater.inflate(R.layout.phrases_list_item, null));
        }

        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {
            // retrieve the text view
            PhraseViewHolder itemHolder = (PhraseViewHolder) holder;
            TextView view = itemHolder.textView;
            // replace text contents
            view.setText(phrases.get(position).getName());
            // replace list item's metadata
            holder.itemView.setTag(position);
        }


        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return phrases.size();
        }

}
