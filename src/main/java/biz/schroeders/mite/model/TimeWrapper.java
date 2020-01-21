package biz.schroeders.mite.model;

import com.google.gson.annotations.SerializedName;

public class TimeWrapper {
    @SerializedName("time_entry")
    private final MiteTime timeEntry;

    public TimeWrapper(final MiteTime timeEntry) {
        this.timeEntry = timeEntry;
    }

    public MiteTime getTimeEntry() {
        return timeEntry;
    }
}
