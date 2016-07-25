package tac.android.de.truckcompanion.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;
import tac.android.de.truckcompanion.fragment.MainFragment;
import tac.android.de.truckcompanion.fragment.MapFragment;
import tac.android.de.truckcompanion.fragment.StatsFragment;


/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:19
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


    /**
     * Instantiates a new View pager adapter.
     *
     * @param supportFragmentManager the support fragment manager
     */
    public ViewPagerAdapter(FragmentManager supportFragmentManager) {
        super(supportFragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
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

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    /**
     * Gets the registered fragment references.
     *
     * @param position the position
     * @return the registered fragment
     */
    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
