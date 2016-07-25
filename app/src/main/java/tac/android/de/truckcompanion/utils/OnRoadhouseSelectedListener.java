package tac.android.de.truckcompanion.utils;

import tac.android.de.truckcompanion.data.Break;
import tac.android.de.truckcompanion.data.Roadhouse;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.wheel.WheelEntry;

/**
 * Created by Jonas Miederer.
 * Date: 16.07.2016
 * Time: 20:17
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public interface OnRoadhouseSelectedListener {

    /**
     * On main fragment roadhouse changed.
     *
     * @param entry the entry
     */
    void onMainFragmentRoadhouseChanged(WheelEntry entry);

    /**
     * On map fragment roadhouse changed.
     *
     * @param entry     the entry
     * @param roadhouse the roadhouse
     */
    void onMapFragmentRoadhouseChanged(WheelEntry entry, Roadhouse roadhouse);

    /**
     * On pause data changed.
     *
     * @param entry the entry
     */
    void onPauseDataChanged(WheelEntry entry);

    /**
     * On break finished.
     */
    void onBreakFinished();

    /**
     * On route changed.
     *
     * @param routeWrapper the route wrapper
     */
    void onRouteChanged(RouteWrapper routeWrapper);
}
