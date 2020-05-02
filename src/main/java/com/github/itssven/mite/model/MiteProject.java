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

import java.time.Duration;

import com.google.gson.annotations.SerializedName;

import static com.github.itssven.mite.model.Project.TYPE_NORMAL;
import static com.github.itssven.mite.model.Project.TYPE_RECURRING;

public class MiteProject {
    private final Integer id;
    private final String name;
    @SerializedName("customer_id")
    private final Integer customerId;
    @SerializedName("customer_name")
    private final String customerName;
    private final Long budget;
    private final Boolean archived;

    public MiteProject(final Integer id, final String name, final Integer customerId, final String customerName,
                       final Long budget, final Boolean archived) {
        this.id = id;
        this.name = name;
        this.customerId = customerId;
        this.customerName = customerName;
        this.budget = budget;
        this.archived = archived;
    }

    public Project toProject() {
        return new Project(id,
                name,
                customerId,
                customerName,
                Duration.ofMinutes(budget),
                archived,
                budget==0 ? TYPE_RECURRING : TYPE_NORMAL,
                0,
                0);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public long getBudget() {
        return budget;
    }
}
