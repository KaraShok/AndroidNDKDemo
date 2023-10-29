package com.karashok.opengldemo.filter;

import android.content.Context;

import com.karashok.opengldemo.R;

/**
 * @author karashok
 * @since 04-25-2023
 */
public class ScreenFilter extends AbstractFilter{

    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_frag);
    }
}
