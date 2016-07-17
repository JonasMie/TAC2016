package tac.android.de.truckcompanion.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Jonas Miederer.
 * Date: 16.07.2016
 * Time: 19:59
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class CustomViewPager extends ViewPager {
    private boolean enabled;

    public CustomViewPager(Context context) {
        super(context);
        this.enabled = true;
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return enabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return enabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPagingEnabled() {
        return enabled;
    }
}
