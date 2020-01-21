package biz.schroeders.mite.model;

import java.time.Duration;

import com.google.gson.annotations.SerializedName;

public class MiteProject {
    private final Integer id;
    private final String name;
    @SerializedName("customer_id")
    private final int customerId;
    @SerializedName("customer_name")
    private final String customerName;
    private final long budget;

    public MiteProject(final Integer id, final String name, final int customerId, final String customerName,
                       final long budget) {
        this.id = id;
        this.name = name;
        this.customerId = customerId;
        this.customerName = customerName;
        this.budget = budget;
    }

    public Project toProject() {
        return new Project(id, name, customerId, customerName, Duration.ofMinutes(budget));
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
