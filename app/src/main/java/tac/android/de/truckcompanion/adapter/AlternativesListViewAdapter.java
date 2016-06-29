package tac.android.de.truckcompanion.adapter;

/**
 * Created by Michael on 19.06.2016.
 */



        import java.util.ArrayList;
        import java.util.HashMap;
        import android.app.Activity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.TextView;

        import tac.android.de.truckcompanion.R;

public class AlternativesListViewAdapter extends BaseAdapter{

    public ArrayList<HashMap<String, String>> list;
    Activity activity;
    TextView txtFirst;
    TextView txtSecond;
    TextView txtThird;
    TextView txtFourth;
    public AlternativesListViewAdapter(Activity activity){
        super();
        this.activity=activity;
        String FIRST_COLUMN="a";
        String SECOND_COLUMN="b";
        String THIRD_COLUMN="c";
       this.list=new ArrayList<HashMap<String,String>>();
        HashMap<String,String> temp=new HashMap<String, String>();
        temp.put(FIRST_COLUMN, "Rasthof Motivationslos");
        temp.put(SECOND_COLUMN, "11.00");
        temp.put(THIRD_COLUMN, "100km");

        list.add(temp);

        HashMap<String,String> temp2=new HashMap<String, String>();
        temp2.put(FIRST_COLUMN, "Raststätte Tac");
        temp2.put(SECOND_COLUMN, "12.00");
        temp2.put(THIRD_COLUMN, "25km");
        list.add(temp2);

        HashMap<String,String> temp3=new HashMap<String, String>();
        temp3.put(FIRST_COLUMN, "Rathof Haumichblau");
        temp3.put(SECOND_COLUMN, "13.30");
        temp3.put(THIRD_COLUMN, "80km");

        list.add(temp3);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub



        LayoutInflater inflater=activity.getLayoutInflater();

        if(convertView == null){

            convertView=inflater.inflate(R.layout.alternative_row, null);

            txtFirst=(TextView) convertView.findViewById(R.id.nameAlt);
            txtSecond=(TextView) convertView.findViewById(R.id.ankunftAlt);
            txtThird=(TextView) convertView.findViewById(R.id.entfernungAlt);
            //txtFourth=(TextView) convertView.findViewById(R.id.status);

        }

        HashMap<String, String> map=list.get(position);
        txtFirst.setText(map.get("a"));
        txtSecond.setText(map.get("b"));
        txtThird.setText(map.get("c"));
       // txtFourth.setText(map.get(FOURTH_COLUMN));

        return convertView;
    }

}