package tac.android.de.truckcompanion.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import tac.android.de.truckcompanion.fragment.MainFragment;
import tac.android.de.truckcompanion.fragment.MapFragment;
import tac.android.de.truckcompanion.fragment.StatsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:19
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public ViewPagerAdapter(FragmentManager supportFragmentManager) {
        super(supportFragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new MainFragment();
            case 1:
                return new MapFragment();
            case 2:
                return new StatsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}
