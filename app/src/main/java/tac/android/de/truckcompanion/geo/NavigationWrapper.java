package tac.android.de.truckcompanion.geo;

import com.here.android.mpa.guidance.NavigationManager;

/**
 * Created by Jonas Miederer.
 * Date: 17.07.2016
 * Time: 21:28
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class NavigationWrapper {

    private static NavigationWrapper navigationWrapper;
    private NavigationManager navigationManager;

    private NavigationWrapper() {
        navigationManager = NavigationManager.getInstance();
    }

    public static NavigationWrapper getInstance() {
        if (navigationWrapper == null) {
            navigationWrapper = new NavigationWrapper();
        }
        return navigationWrapper;
    }

    public NavigationManager getNavigationManager() {
        return navigationManager;
    }

    public void setNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }
}
