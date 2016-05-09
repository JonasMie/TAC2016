package tac.android.de.truckcompanion.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import tac.android.de.truckcompanion.fragment.MainFragment;

/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:19
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(FragmentManager supportFragmentManager) {
        super(supportFragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new MainFragment();  // TODO: create fragment
//                return new MapFragment();
            case 2:
                return new MainFragment(); // TODO: create fragment
//                return new StatsFragment();
            default:
                return new MainFragment();
        }

    }

    @Override
    public int getCount() {
        return 3;
    }
}
