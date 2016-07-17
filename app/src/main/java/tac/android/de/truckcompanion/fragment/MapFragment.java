package tac.android.de.truckcompanion.fragment;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.search.PlaceLink;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Break;
import tac.android.de.truckcompanion.data.Roadhouse;
import tac.android.de.truckcompanion.utils.OnRoadhouseSelectedListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 17:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MapFragment extends Fragment implements MapGesture.OnGestureListener {

    private static final String MAP_TAG = "map_tag";
    private static final String TAG = MapFragment.class.getSimpleName();
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

    private Image icon_main;
    private Image icon_alt;

    // this may be the ugliest thing i've ever written
    private HashMap<MapMarker, EntryRoadhouseStruct> markerWheelEntryMap;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        markerWheelEntryMap = new HashMap<>();


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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnRoadhouseSelectedListener) context;

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
        setSidebarInfo(entry);
    }

    public void setSidebarInfo(WheelEntry entry) {
        setSidebarInfo(entry, entry.getPause().getMainRoadhouse());
    }

    public void setSidebarInfo(WheelEntry entry, Roadhouse roadhouse) {
        PlaceLink placeLink = roadhouse.getPlaceLink();

        map.setCenter(placeLink.getPosition(), Map.Animation.BOW);
        rh_title.setText(placeLink.getTitle());
        rh_address.setText("01234 Adresse");
        rh_rating.setRating((float) placeLink.getAverageRating());
        if (roadhouse.getETA() != null) {
            rh_eta.setText(dateFormat.format(roadhouse.getETA()));
        } else {// TODO
            rh_eta.setText("12:30");
        }
        rh_distance.setText("20 km"); // TODO
        rh_breaktime.setText(((int) entry.getVal() / 60) + " min");

    }

    public void addMarkerCluster(WheelEntry entry) {
        if (icon_main == null || icon_alt == null) {
            icon_main = new Image();
            icon_alt = new Image();
            try {
                icon_main.setImageResource(R.drawable.marker_main);
                icon_alt.setImageResource(R.drawable.marker_alt);
            } catch (IOException e) {
                Log.e(TAG, "Marker image not found");
            }

        }

        Break pause = entry.getPause();
        // TODO: remove from hashmap
        if (pause.getClusterLayer() != null) {
            map.removeClusterLayer(pause.getClusterLayer());
        }

        ClusterLayer cl = new ClusterLayer();
        if (pause.getMainRoadhouse() != null) {
            MapMarker marker = new MapMarker();
            marker.setIcon(icon_main);
            marker.setCoordinate(pause.getMainRoadhouse().getPlaceLink().getPosition());
            markerWheelEntryMap.put(marker, new EntryRoadhouseStruct(entry, pause.getMainRoadhouse()));
            cl.addMarker(marker);
        }
        if (pause.getAlternativeRoadhouses() != null) {
            for (Roadhouse rh : pause.getAlternativeRoadhouses()) {
                MapMarker marker = new MapMarker();
                marker.setIcon(icon_alt);
                marker.setCoordinate(rh.getPlaceLink().getPosition());
                markerWheelEntryMap.put(marker, new EntryRoadhouseStruct(entry, rh));
                cl.addMarker(marker);
            }
        }
        pause.setClusterLayer(cl);
        map.addClusterLayer(cl);
    }

    @Override
    public void onPanStart() {

    }

    @Override
    public void onPanEnd() {

    }

    @Override
    public void onMultiFingerManipulationStart() {

    }

    @Override
    public void onMultiFingerManipulationEnd() {

    }

    @Override
    public boolean onMapObjectsSelected(List<ViewObject> objects) {
        for (ViewObject viewObj : objects) {
            if (viewObj.getBaseType() == ViewObject.Type.USER_OBJECT) {
                if (((MapObject) viewObj).getType() == MapObject.Type.MARKER) {
                    EntryRoadhouseStruct struct = markerWheelEntryMap.get(viewObj);
                    setSidebarInfo(struct.entry, struct.rh);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTapEvent(PointF pointF) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(PointF pointF) {
        return false;
    }

    @Override
    public void onPinchLocked() {

    }

    @Override
    public boolean onPinchZoomEvent(float v, PointF pointF) {
        return false;
    }

    @Override
    public void onRotateLocked() {

    }

    @Override
    public boolean onRotateEvent(float v) {
        return false;
    }

    @Override
    public boolean onTiltEvent(float v) {
        return false;
    }

    @Override
    public boolean onLongPressEvent(PointF pointF) {
        return false;
    }

    @Override
    public void onLongPressRelease() {

    }

    @Override
    public boolean onTwoFingerTapEvent(PointF pointF) {
        return false;
    }

    private class EntryRoadhouseStruct {
        public WheelEntry entry;
        public Roadhouse rh;


        public EntryRoadhouseStruct(WheelEntry entry, Roadhouse rh) {
            this.entry = entry;
            this.rh = rh;
        }
    }

}
