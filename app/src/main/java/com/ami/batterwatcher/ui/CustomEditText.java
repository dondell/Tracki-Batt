package com.ami.batterwatcher.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

/**
 * Created by Reaston3 on 4/21/2017.
 */

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {


    private Context context;
    private AttributeSet attrs;
    private int defStyle;
    private Drawable backgroundDrawable;

    public CustomEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "vodafone_rg_r.ttf");
        this.setTypeface(font);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "vodafone_rg_r.ttf");
        super.setTypeface(tf, style);
    }

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "vodafone_rg_r.ttf");
        super.setTypeface(tf);
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        this.backgroundDrawable = ContextCompat.getDrawable(context, resId);
        super.setBackgroundResource(resId);
    }

    @Override
    public Drawable getBackground() {
        if (backgroundDrawable != null){
            return backgroundDrawable;
        } else {
            return super.getBackground();
        }
    }
}