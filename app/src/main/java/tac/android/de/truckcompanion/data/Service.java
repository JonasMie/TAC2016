package tac.android.de.truckcompanion.data;

import java.sql.Array;

/**
 * Created by Michael on 17.05.2016.
 */
public class Service {
    private int id;
    private String name;
    private double gasprice;
    private float rating;
    private int ratingCount;
    private String[] servicelist = new String[20];

    public Service(){
        //Todo load dynamicly
        this.name="Autohof Obertulpach";
        this.gasprice=1.22;
        this.rating=2;
        this.ratingCount=100;
        this.servicelist[0]="Restaurant";
        this.servicelist[1]="Internet Cafe";
    }
    public String getName(){
        return this.name;
         }
    public double getGasprice(){
        return this.gasprice;
    }
    public float getRating(){
        return this.rating;
    }
    public int getRatingCount(){
        return this.ratingCount;
    }
    public String[] getServicelist(){
        return this.servicelist;
    }
}
