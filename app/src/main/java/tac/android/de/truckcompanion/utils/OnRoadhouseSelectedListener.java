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

    void onMainFragmentRoadhouseChanged(WheelEntry entry);

    void onMapFragmentRoadhouseChanged(WheelEntry entry, Roadhouse roadhouse);

    void onPauseDataChanged(WheelEntry entry);

    void onBreakFinished();

    void onRouteChanged(RouteWrapper routeWrapper);
}
