package biz.schroeders.mite.model;

public class CustomerWrapper {
    private final Customer customer;

    public CustomerWrapper(final Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }
}
