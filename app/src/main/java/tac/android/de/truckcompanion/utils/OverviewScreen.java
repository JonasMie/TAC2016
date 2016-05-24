package tac.android.de.truckcompanion.utils;

import android.view.View;
import android.widget.TextView;


import java.sql.Array;
import java.util.ArrayList;

import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Service;

/**
 * Created by Michael on 17.05.2016.
 */
public class OverviewScreen {
    private TextView name;
    private int rating;

    private TextView distance;
    private TextView breakDuration;
    private TextView arraveAt;
    private View view;
    private Service data;
    private TextView ratingCount;
    /** Loads Data**/
    public OverviewScreen (Service s, View v){
        this.view = v;
        this.data = s;
        this.viewInformation();
        }
    public void viewInformation(){
        this.name = (TextView) this.view.findViewById(R.id.NameOfService);
        this.name.setText(data.getName());
        //(TextView)(this.view.findViewById(R.id.ratingNumber)).setText(data.getRatingCount());

    }


}
