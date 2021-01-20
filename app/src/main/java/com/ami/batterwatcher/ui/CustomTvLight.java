package com.ami.batterwatcher.ui;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Reaston3 on 4/4/2017.
 */

public class CustomTvLight extends androidx.appcompat.widget.AppCompatTextView {

    public CustomTvLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustomTvLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTvLight(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "vodafone_rg_r.ttf");
            setTypeface(tf);
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if(focused)
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if(focused)
            super.onWindowFocusChanged(focused);
    }


    @Override
    public boolean isFocused() {
        return true;
    }
}