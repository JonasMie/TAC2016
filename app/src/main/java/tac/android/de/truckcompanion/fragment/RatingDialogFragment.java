package tac.android.de.truckcompanion.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;

/**
 * Created by Jonas Miederer.
 * Date: 19.07.2016
 * Time: 21:25
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class RatingDialogFragment extends DialogFragment {

    private AlertDialog dialog;
    private RatingBar ratingBar;

    public static RatingDialogFragment newInstance(int title, String roadhouse_title) {
        RatingDialogFragment fragment = new RatingDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("roadhouse_title", roadhouse_title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        String roadhouse_title = getArguments().getString("roadhouse_title");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_rate, null);

        TextView textView = (TextView) layout.findViewById(R.id.rating_roadhouse_query_text);
        textView.setText(String.format(getResources().getString(R.string.roadhouse_query_text), roadhouse_title));

        ratingBar = (RatingBar) layout.findViewById(R.id.rating_rating);
        dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(layout)
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity) getActivity()).doPositiveClick();
                            }
                        })
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity) getActivity()).doNegativeClick();
                            }
                        })
                .setNeutralButton(R.string.alert_dialog_rate_more,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity) getActivity()).doRateMore();
                            }
                        })
                .create();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        final Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        positiveButton.setEnabled(false);
        neutralButton.setEnabled(false);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                positiveButton.setEnabled(true);
                neutralButton.setEnabled(true);
            }
        });

    }
}
