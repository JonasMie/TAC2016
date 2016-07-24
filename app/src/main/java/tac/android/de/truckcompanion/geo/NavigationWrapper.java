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

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static NavigationWrapper getInstance() {
        if (navigationWrapper == null) {
            navigationWrapper = new NavigationWrapper();
        }
        return navigationWrapper;
    }

    /**
     * Gets navigation manager.
     *
     * @return the navigation manager
     */
    public NavigationManager getNavigationManager() {
        return navigationManager;
    }

    /**
     * Sets navigation manager.
     *
     * @param navigationManager the navigation manager
     */
    public void setNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }
}
