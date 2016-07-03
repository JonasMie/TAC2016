package tac.android.de.truckcompanion.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import tac.android.de.truckcompanion.R;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 17:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment map;
    GoogleMap gMap = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        map =  (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        map.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;
        LatLng hdm = new LatLng(48.742,9.098);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(hdm, 13));

        map.addMarker(new MarkerOptions()
                .title("HdM")
                .snippet("Hochschule der Medien")
                .position(hdm));
    }
}
