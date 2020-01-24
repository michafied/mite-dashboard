package biz.schroeders.mite.model;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.request.Request;

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

    public static void register(final Pattern projectNameMatcher) {
        Project.projectNameMatcher = projectNameMatcher;
    }

    public MiteProject toMite() {
        final Long nullableBudget;
        if (budget != null) {
            nullableBudget = Duration.ofHours(budget).toMinutes();
        } else {
            nullableBudget = null;
        }

        return new MiteProject(id,
                name,
                customerId,
                customerName,
                nullableBudget,
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
}
