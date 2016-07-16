package tac.android.de.truckcompanion.fragment;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.search.PlaceLink;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Break;
import tac.android.de.truckcompanion.data.Roadhouse;
import tac.android.de.truckcompanion.utils.OnRoadhouseSelectedListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    private OnRoadhouseSelectedListener listener;

    private ScrollView sidebar;
    private TextView rh_title;
    private TextView rh_address;
    private TextView rh_rating_label;
    private TextView rh_eta;
    private TextView rh_distance;
    private TextView rh_breaktime;
    private RatingBar rh_rating;
    private ImageView rh_image;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (mapFragment == null) {
            mapFragment = (com.here.android.mpa.mapping.MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        }

        sidebar = (ScrollView) view.findViewById(R.id.map_rec_sidebar);
        rh_title = (TextView) view.findViewById(R.id.map_rec_title);
        rh_address = (TextView) view.findViewById(R.id.map_rec_address);
        rh_rating_label = (TextView) view.findViewById(R.id.map_rec_rating_label);
        rh_eta = (TextView) view.findViewById(R.id.map_rec_info_eta);
        rh_distance = (TextView) view.findViewById(R.id.map_rec_info_distance);
        rh_breaktime = (TextView) view.findViewById(R.id.map_rec_info_breaktime);
        rh_rating = (RatingBar) view.findViewById(R.id.map_rec_rating);
        rh_image = (ImageView) view.findViewById(R.id.map_rec_img);
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

    public void setMainRoadhouse(WheelEntry entry) {
        Break pause = entry.getPause();
        PlaceLink placeLink = pause.getMainRoadhouse().getPlaceLink();

        rh_title.setText(placeLink.getTitle());
        rh_address.setText("01234 Adresse");
        rh_rating.setRating((float) placeLink.getAverageRating());
        if (pause.getMainRoadhouse().getETA() != null) {
            rh_eta.setText(dateFormat.format(pause.getMainRoadhouse().getETA()));
        } else {// TODO
            rh_eta.setText("12:30");
        }
        rh_distance.setText("20 km"); // TODO
        rh_breaktime.setText(((int) entry.getVal() / 60) + " min");

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnRoadhouseSelectedListener) context;

    }
}
