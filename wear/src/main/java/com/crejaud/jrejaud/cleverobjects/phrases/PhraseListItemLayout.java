package com.crejaud.jrejaud.cleverobjects.phrases;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crejaud.jrejaud.cleverobjects.R;

/**
 * Created by jrejaud on 6/29/15.
 */
public class PhraseListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {


        //private ImageView mCircle;
        private TextView mName;

        private final float mFadedTextAlpha;
        private final int mFadedCircleColor;
        private final int mChosenCircleColor;

        public PhraseListItemLayout(Context context) {
            this(context, null);
        }

        public PhraseListItemLayout(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public PhraseListItemLayout(Context context, AttributeSet attrs,
                                      int defStyle) {
            super(context, attrs, defStyle);

            mFadedTextAlpha = 100;
            mFadedCircleColor = getResources().getColor(R.color.grey);
            mChosenCircleColor = getResources().getColor(R.color.blue);
        }

        // Get references to the icon and text in the item layout definition
        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            // These are defined in the layout file for list items
            // (see next section)
            //mCircle = (ImageView) findViewById(R.id.circle);
            mName = (TextView) findViewById(R.id.name);
        }

        @Override
        public void onCenterPosition(boolean animate) {
            mName.setAlpha(31); //1f
            mName.setTextSize(getResources().getDimension(R.dimen.phrase_text_selected));
            //((GradientDrawable) mCircle.getDrawable()).setColor(mChosenCircleColor);
        }

        @Override
        public void onNonCenterPosition(boolean animate) {
            //((GradientDrawable) mCircle.getDrawable()).setColor(mFadedCircleColor);
            mName.setAlpha(mFadedTextAlpha);
            mName.setTextSize(getResources().getDimension(R.dimen.phrase_text_unselected));
        }

}
