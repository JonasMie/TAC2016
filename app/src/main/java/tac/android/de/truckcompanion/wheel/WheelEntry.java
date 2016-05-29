package tac.android.de.truckcompanion.wheel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import com.github.mikephil.charting.data.Entry;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;

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

    public static final ArrayList<Integer> COLORS = new ArrayList<Integer>() {
        {
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelDriveEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelPauseEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelRecoveryEntry));
        }
    };

    private int entryType;
    private boolean editModeActive;
    private static WheelEntry activeEntry;

    public WheelEntry(float val, int xIndex) {
        super(val, xIndex);
    }

    public WheelEntry(float val, int xIndex, int entryType) {
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

    public static ArrayList<Entry> getEntries() {
        return new ArrayList<Entry>() {
            {
                add(new WheelEntry(270, 0, DRIVE_ENTRY));
                add(new WheelEntry(45, 1, PAUSE_ENTRY));
                add(new WheelEntry(200, 2, DRIVE_ENTRY));
                add(new WheelEntry(15, 3, PAUSE_ENTRY));
                add(new WheelEntry(70, 4, DRIVE_ENTRY));
                add(new WheelEntry(30, 5, PAUSE_ENTRY));
                add(new WheelEntry(90, 6, RECOVERY_ENTRY));
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
