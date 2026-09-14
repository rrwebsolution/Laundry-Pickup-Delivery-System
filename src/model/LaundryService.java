package model;

import java.math.BigDecimal;

public class LaundryService {

    private int serviceId;
    private String serviceName;
    private String description;
    private BigDecimal price;
    private PricingType pricingType;

    public LaundryService() {
    }

    public LaundryService(int serviceId, String serviceName, String description, BigDecimal price,
            PricingType pricingType) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.pricingType = pricingType;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PricingType getPricingType() {
        return pricingType;
    }

    public void setPricingType(PricingType pricingType) {
        this.pricingType = pricingType;
    }

    @Override
    public String toString() {
        return serviceName + " (" + pricingType.getLabel() + " - PHP " + price + ")";
    }
}
