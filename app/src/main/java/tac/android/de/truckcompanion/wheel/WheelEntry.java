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

    /**
     * The constant DRIVE_ENTRY.
     */
    public static final int DRIVE_ENTRY = 0;
    /**
     * The constant PAUSE_ENTRY.
     */
    public static final int PAUSE_ENTRY = 1;
    /**
     * The constant RECOVERY_ENTRY.
     */
    public static final int RECOVERY_ENTRY = 2;
    /**
     * The constant BUFFER_ENTRY.
     */
    public static final int BUFFER_ENTRY = 3;

    /**
     * The constant COLORS.
     */
    public static final ArrayList<Integer> COLORS = new ArrayList<Integer>() {
        {
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelDriveEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelPauseEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelRecoveryEntry));
            add(ContextCompat.getColor(MainActivity.context, R.color.wheelDriveEntry));
        }
    };

    private static WheelEntry activeEntry;
    private Break pause;
    private int entryType;
    private boolean editModeActive;
    private float stepAngle;

    /**
     * Instantiates a new Wheel entry.
     *
     * @param val    the val
     * @param xIndex the x index
     */
    public WheelEntry(float val, int xIndex) {
        super(val, xIndex);
    }

    /**
     * Instantiates a new Wheel entry.
     *
     * @param val         the val
     * @param xIndex      the x index
     * @param entryType   the entry type
     * @param elapsedTime the elapsed time
     * @param pauseIndex  the pause index
     */
    public WheelEntry(float val, int xIndex, int entryType, int elapsedTime, int pauseIndex) {
        super(val, xIndex);
        this.entryType = entryType;
        this.editModeActive = false;
        if (this.entryType == PAUSE_ENTRY) {
            this.pause = new Break(elapsedTime, pauseIndex, this);
        }
    }

    /**
     * Instantiates a new Wheel entry.
     *
     * @param val         the val
     * @param xIndex      the x index
     * @param entryType   the entry type
     * @param elapsedTime the elapsed time
     * @param pauseIndex  the pause index
     * @param addBreak    the add break
     */
    public WheelEntry(float val, int xIndex, int entryType, int elapsedTime, int pauseIndex, boolean addBreak) {
        super(val, xIndex);
        this.entryType = entryType;
        this.editModeActive = false;
        if (this.entryType == PAUSE_ENTRY && addBreak) {
            this.pause = new Break(elapsedTime, pauseIndex, this);
        }
    }

    /**
     * Instantiates a new Wheel entry.
     *
     * @param val         the val
     * @param xIndex      the x index
     * @param entryType   the entry type
     * @param elapsedTime the elapsed time
     */
    public WheelEntry(float val, int xIndex, int entryType, int elapsedTime) {
        super(val, xIndex);
        this.entryType = entryType;
        this.editModeActive = false;
    }

    /**
     * Instantiates a new Wheel entry.
     *
     * @param val    the val
     * @param xIndex the x index
     * @param data   the data
     */
    public WheelEntry(float val, int xIndex, Object data) {
        super(val, xIndex, data);
    }

    /**
     * Instantiates a new Wheel entry.
     *
     * @param in the in
     */
    protected WheelEntry(Parcel in) {
        super(in);
    }

    /**
     * The constant CREATOR.
     */
    public static final Parcelable.Creator<WheelEntry> CREATOR = new Parcelable.Creator<WheelEntry>() {
        public WheelEntry createFromParcel(Parcel source) {
            return new WheelEntry(source);
        }

        public WheelEntry[] newArray(int size) {
            return new WheelEntry[size];
        }
    };

    /**
     * Gets predefined default entries.
     *
     * @return the entries
     */
    public static ArrayList<Entry> getEntries() {
        return new ArrayList<Entry>() {
            {
                add(new WheelEntry(270*60, 0, DRIVE_ENTRY, 0));
                add(new WheelEntry(45*60, 1, PAUSE_ENTRY, 270*60, 0));
                add(new WheelEntry(270*60, 2, DRIVE_ENTRY, 315*60));
                add(new WheelEntry(45*60, 3, PAUSE_ENTRY, 585*60, 1));
                add(new WheelEntry(60*60, 4, DRIVE_ENTRY, 650*60));
                add(new WheelEntry(30*60, 5, RECOVERY_ENTRY, 710*60));
            }
        };
    }

    /**
     * Gets colors for the entries.
     *
     * @param entries the entries
     * @return the colors
     */
    public static ArrayList<Integer> getColors(ArrayList<Entry> entries) {

        ArrayList<Integer> colors = new ArrayList<>();
        for (Entry entry : entries) {
            colors.add(COLORS.get(((WheelEntry) entry).getEntryType()));
        }
        return colors;
    }

    /**
     * Gets active entry.
     *
     * @return the active entry
     */
    public static WheelEntry getActiveEntry() {
        return activeEntry;
    }

    /**
     * Gets entry type.
     *
     * @return the entry type
     */
    public int getEntryType() {
        return entryType;
    }

    /**
     * Is edit mode active boolean.
     *
     * @return the boolean
     */
    public boolean isEditModeActive() {
        return editModeActive;
    }

    /**
     * Sets edit mode active.
     *
     * @param editModeActive the edit mode active
     */
    public void setEditModeActive(boolean editModeActive) {
        if (this.getEntryType() == PAUSE_ENTRY) {
            this.editModeActive = editModeActive;
            activeEntry = editModeActive ? this : null;
        }
    }

    /**
     * Gets pause.
     *
     * @return the pause
     */
    public Break getPause() {
        return pause;
    }

    /**
     * Sets pause.
     *
     * @param pause the pause
     */
    public void setPause(Break pause) {
        this.pause = pause;
    }


    /**
     * Gets step angle.
     *
     * @return the step angle
     */
    public float getStepAngle() {
        return stepAngle;
    }

    /**
     * Sets step angle.
     *
     * @param stepAngle the step angle
     */
    public void setStepAngle(float stepAngle) {
        this.stepAngle = stepAngle;
    }

}
