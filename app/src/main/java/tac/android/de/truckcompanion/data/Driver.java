package tac.android.de.truckcompanion.data;

import tac.android.de.truckcompanion.logic.LogicHelper;

/**
 * Created by Jonas Miederer.
 * Date: 26.05.2016
 * Time: 14:53
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Driver {
    private int id;
    private LogicHelper workingHours;

    public Driver(int id) {
        this.id= id;
        workingHours = new LogicHelper();
    }
}
