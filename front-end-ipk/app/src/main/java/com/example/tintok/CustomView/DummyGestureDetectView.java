package com.example.tintok.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class DummyGestureDetectView extends LinearLayout {


    public DummyGestureDetectView(Context context) {
        super(context);
    }

    public DummyGestureDetectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DummyGestureDetectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DummyGestureDetectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*if(mListner!= null)
            mListner.onMotionEventIntercepted(ev);*/
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(mListner!= null)
            mListner.onMotionEventIntercepted(ev);
        return super.dispatchTouchEvent(ev);
    }

    public onMotionEventIntercepted mListner;
    public interface onMotionEventIntercepted{
        public boolean onMotionEventIntercepted(MotionEvent ev);
    }
}
