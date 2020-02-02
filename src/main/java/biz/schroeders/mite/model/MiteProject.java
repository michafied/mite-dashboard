package biz.schroeders.mite.model;

import java.time.Duration;

import com.google.gson.annotations.SerializedName;

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
