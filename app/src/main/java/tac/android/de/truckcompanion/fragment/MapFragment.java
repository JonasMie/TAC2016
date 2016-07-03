package tac.android.de.truckcompanion.fragment;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import tac.android.de.truckcompanion.R;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 17:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MapFragment extends Fragment {

    private static final String MAP_TAG = "map_tag";
    private Map map;
    private com.here.android.mpa.mapping.MapFragment mapFragment;
    private LinearLayout container;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (mapFragment == null) {
            mapFragment = (com.here.android.mpa.mapping.MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        }

        return view;
    }

    public void init(OnEngineInitListener callback) {
        mapFragment.init(callback);
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public com.here.android.mpa.mapping.MapFragment getMapFragment() {
        return mapFragment;
    }

    public void setMapFragment(com.here.android.mpa.mapping.MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }
}
