package biz.schroeders.mite.model;

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
