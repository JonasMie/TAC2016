package tac.android.de.truckcompanion.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import tac.android.de.truckcompanion.R;


/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:37
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MyMapFragment extends Fragment {

    private TextView textView;
    private RelativeLayout mRelativeLayout;
    private PieChart mChart;
    MapView mapView;
    GoogleMap map;

    private GoogleApiClient.Builder mGoogleApiClient;



    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
      //  map.getUiSettings().setMyLocationButtonEnabled(false);
        //map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
       // try {
          //  MapsInitializer.initialize(this.getActivity());
       // } catch (GooglePlayServicesNotAvailableException e) {
        //    e.printStackTrace();
        //}

        // Updates the location and zoom of the MapView
       // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
       // map.animateCamera(cameraUpdate);


        return view;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
