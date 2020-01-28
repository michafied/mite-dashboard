package biz.schroeders.mite.model;

import java.time.Duration;

public class Time {
    private final String serviceName;
    private final long hours;

    public Time(final Duration duration) {
        this(null, duration);
    }

    public Time(final String serviceName, final Duration duration) {
        this.serviceName = serviceName;
        hours = duration.toHours();
    }

    public long getHours() {
        return hours;
    }

    public String getServiceName() {
        return serviceName;
    }
}
