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

    /**
     * Instantiates a new Custom view pager.
     *
     * @param context the context
     */
    public CustomViewPager(Context context) {
        super(context);
        this.enabled = true;
    }

    /**
     * Instantiates a new Custom view pager.
     *
     * @param context the context
     * @param attrs   the attrs
     */
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

    /**
     * Sets paging enabled.
     *
     * @param enabled the enabled
     */
    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Is paging enabled boolean.
     *
     * @return the boolean
     */
    public boolean isPagingEnabled() {
        return enabled;
    }
}
