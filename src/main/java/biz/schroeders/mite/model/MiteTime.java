package biz.schroeders.mite.model;

import com.google.gson.annotations.SerializedName;

public class MiteTime {
    @SerializedName("service_name")
    private final String serviceName;
    private final Integer minutes;

    public MiteTime(final String serviceName, final Integer minutes) {
        this.serviceName = serviceName;
        this.minutes = minutes;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public String getServiceName() {
        return serviceName;
    }
}
