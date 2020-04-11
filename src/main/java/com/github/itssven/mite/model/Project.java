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
import java.util.Optional;
import java.util.regex.Pattern;

import com.github.itssven.mite.ApiError;
import com.github.itssven.mite.constants.HttpCodes;
import com.github.itssven.mite.request.Request;

public class Project implements Request<Project> {
    private static Pattern projectNameMatcher = Pattern.compile(".+");
    private final Integer id;
    private final String name;
    private final Integer customerId;
    private final String customerName;
    private final Long budget;
    private final Boolean archived;

    private final Integer boundTo;
    private final Integer sorting;

    public Project(final Integer id, final String name, final Integer customerId, final String customerName,
                   final Duration budget, final Boolean archived, final Integer boundTo, final Integer sorting) {
        this.id = id;
        this.name = name;
        this.customerId = customerId;
        this.customerName = customerName;
        this.budget = Long.valueOf(budget.toHours());
        this.archived = archived;
        this.boundTo = boundTo;
        this.sorting = sorting;
    }

    private Project(final Builder builder) {
        id = builder.id;
        name = builder.name;
        customerId = builder.customerId;
        customerName = builder.customerName;
        budget = builder.budget;
        archived = builder.archived;
        boundTo = builder.boundTo;
        sorting = builder.sorting;
    }

    public static void register(final Pattern projectNameMatcher) {
        Project.projectNameMatcher = projectNameMatcher;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final Project copy) {
        final Builder builder = new Builder();
        builder.id = copy.getId();
        builder.name = copy.getName();
        builder.customerId = copy.getCustomerId();
        builder.customerName = copy.getCustomerName();
        builder.budget = copy.getBudget();
        builder.archived = copy.getArchived();
        builder.boundTo = copy.getBoundTo().orElse(0);
        builder.sorting = copy.getSorting().orElse(0);
        return builder;
    }

    public MiteProject toArchivable() {
        return new MiteProject(null,
                null,
                null,
                null,
                null,
                archived);
    }

    public MiteProject toMite() {
        return new MiteProject(id,
                name,
                customerId,
                customerName,
                Duration.ofHours(budget).toMinutes(),
                archived);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getBudget() {
        return budget;
    }

    public Boolean getArchived() {
        return archived;
    }

    public Optional<Integer> getBoundTo() {
        return Optional.ofNullable(boundTo);
    }

    public Optional<Integer> getSorting() {
        return Optional.ofNullable(sorting);
    }

    @Override
    public Project validate() {
        if (budget == null || budget < 1) {
            throw new ApiError("Budget needs to be atleast 1h.", HttpCodes.BAD_REQUEST);
        }
        if (name == null || !projectNameMatcher.matcher(name).matches()) {
            throw new ApiError("Name needs to match " + projectNameMatcher, HttpCodes.BAD_REQUEST);
        }
        if (customerId == null || customerId < 0) {
            throw new ApiError("Customer needs to be chosen.", HttpCodes.BAD_REQUEST);
        }
        return this;
    }

    public static final class Builder {
        private Integer id;
        private String name;
        private Integer customerId;
        private String customerName;
        private Long budget;
        private Boolean archived;
        private Integer boundTo;
        private Integer sorting;

        private Builder() {
        }

        public Builder withId(final Integer val) {
            id = val;
            return this;
        }

        public Builder withName(final String val) {
            name = val;
            return this;
        }

        public Builder withCustomerId(final Integer val) {
            customerId = val;
            return this;
        }

        public Builder withCustomerName(final String val) {
            customerName = val;
            return this;
        }

        public Builder withBudget(final Long val) {
            budget = val;
            return this;
        }

        public Builder withArchived(final Boolean val) {
            archived = val;
            return this;
        }

        public Builder withBoundTo(final Integer val) {
            boundTo = val;
            return this;
        }

        public Builder withSorting(final Integer val) {
            sorting = val;
            return this;
        }

        public Project build() {
            return new Project(this);
        }
    }
}
