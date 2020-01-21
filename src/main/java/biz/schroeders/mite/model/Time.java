package biz.schroeders.mite.model;

import java.time.Duration;

public class Time {
    private final long hours;

    public Time(final Duration duration) {
        hours = duration.toHours();
    }

    public long getHours() {
        return hours;
    }
}
