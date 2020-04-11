package com.github.itssven.mite.model;

/*
    This file is part of mite-dashboard.

    mite-dashboard is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mite-dashboard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with mite-dashboard.  If not, see <http://www.gnu.org/licenses/>.
*/

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
