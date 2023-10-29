package com.karashok.opengldemo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;

/**
 * @author karashok
 * @since 05-09-2023
 */
public class RecordButton extends TextView {

    private OnRecordCallback mRecordCallback;

    public RecordButton(Context context) {
        super(context);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setRecordCallback(OnRecordCallback recordCallback) {
        mRecordCallback = recordCallback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRecordCallback == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                mRecordCallback.onStart();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                mRecordCallback.onStop();
                break;
        }
        return true;
    }

    public interface OnRecordCallback {

        void onStart();

        void onStop();
    }
}
