package com.orangutandevelopment.bobsquest;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mitch on 13/10/2015.
 */
public class CoolFontTextView extends TextView {
    public CoolFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "Munro.ttf"));
    }
}
