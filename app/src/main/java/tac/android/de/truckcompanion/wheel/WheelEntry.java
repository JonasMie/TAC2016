package tac.android.de.truckcompanion.wheel;

import android.app.ProgressDialog;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import com.github.mikephil.charting.data.Entry;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Break;

import java.util.ArrayList;

/**
 * Created by Jonas Miederer.
 * Date: 28.05.2016
 * Time: 15:30
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */

public class WheelEntry extends Entry {

    public static final int DRIVE_ENTRY = 0;
    public static final int PAUSE_ENTRY = 1;
    public static final int RECOVERY_ENTRY = 2;
    public static final int BUFFER_ENTRY = 3;

    public static final ArrayList<Integer> COLORS = new ArrayList<Integer>() {
        {
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelDriveEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelPauseEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelRecoveryEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelDriveEntry));
        }
    };

    private int entryType;
    private boolean editModeActive;
    private static WheelEntry activeEntry;
    private Break pause;

    public WheelEntry(float val, int xIndex) {
        super(val, xIndex);
    }

    public WheelEntry(float val, int xIndex, int entryType, int elapsedTime, int pauseIndex) {
        super(val, xIndex);
        this.entryType = entryType;
        this.editModeActive = false;
        if (this.entryType == PAUSE_ENTRY) {
            this.pause = new Break(elapsedTime, pauseIndex);
        }
    }

    public WheelEntry(float val, int xIndex, int entryType, int elapsedTime) {
        super(val, xIndex);
        this.entryType = entryType;
        this.editModeActive = false;
    }

    public WheelEntry(float val, int xIndex, Object data) {
        super(val, xIndex, data);
    }

    protected WheelEntry(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<WheelEntry> CREATOR = new Parcelable.Creator<WheelEntry>() {
        public WheelEntry createFromParcel(Parcel source) {
            return new WheelEntry(source);
        }

        public WheelEntry[] newArray(int size) {
            return new WheelEntry[size];
        }
    };

    public static ArrayList<Entry> getEntries(ProgressDialog mProgressDialog) {
        return new ArrayList<Entry>() {
            {
                add(new WheelEntry(270, 0, DRIVE_ENTRY, 0));
                add(new WheelEntry(45, 1, PAUSE_ENTRY, 270, 0));
                add(new WheelEntry(270, 2, DRIVE_ENTRY, 315));
                add(new WheelEntry(45, 3, PAUSE_ENTRY, 585, 1));
                add(new WheelEntry(60, 6, DRIVE_ENTRY, 650));
                add(new WheelEntry(30, 7, RECOVERY_ENTRY, 710));
            }
        };
    }

    public static ArrayList<Integer> getColors(ArrayList<Entry> entries) {

        ArrayList<Integer> colors = new ArrayList<>();
        for (Entry entry : entries) {
            colors.add(COLORS.get(((WheelEntry) entry).getEntryType()));
        }
        return colors;
    }

    public static WheelEntry getActiveEntry() {
        return activeEntry;
    }

    public int getEntryType() {
        return entryType;
    }

    public boolean isEditModeActive() {
        return editModeActive;
    }

    public void setEditModeActive(boolean editModeActive) {
        if (this.getEntryType() == PAUSE_ENTRY) {
            this.editModeActive = editModeActive;
            activeEntry = editModeActive ? this : null;
        }
    }
}
