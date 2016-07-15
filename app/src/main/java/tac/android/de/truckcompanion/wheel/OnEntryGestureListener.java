package tac.android.de.truckcompanion.wheel;

import android.view.MotionEvent;

/**
 * Created by Jonas Miederer.
 * Date: 01.06.2016
 * Time: 17:11
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public interface OnEntryGestureListener {
    void onEntryDragged(MotionEvent me, float diffAngle);
    void onEntryResized(MotionEvent me);
}
