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
    /**
     * On entry dragged.
     *
     * @param me        the me
     * @param diffAngle the diff angle
     */
    void onEntryDragged(MotionEvent me, float diffAngle);

    /**
     * On entry resized.
     *
     * @param me the me
     */
    void onEntryResized(MotionEvent me);
}
