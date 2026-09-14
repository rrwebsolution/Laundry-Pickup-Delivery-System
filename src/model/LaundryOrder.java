package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LaundryOrder {

    private int orderId;
    private Customer customer;
    private LaundryService service;
    private BigDecimal weightQuantity;
    private BigDecimal price;
    private BigDecimal pickupFee;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private LocalDate expectedCompletionDate;
    private OrderStatus status;
    private String notes;

    public LaundryOrder() {
    }

    public LaundryOrder(int orderId, Customer customer, LaundryService service, BigDecimal weightQuantity,
            BigDecimal price, BigDecimal pickupFee, BigDecimal deliveryFee, BigDecimal totalAmount,
            LocalDateTime orderDate, LocalDate expectedCompletionDate, OrderStatus status, String notes) {
        this.orderId = orderId;
        this.customer = customer;
        this.service = service;
        this.weightQuantity = weightQuantity;
        this.price = price;
        this.pickupFee = pickupFee;
        this.deliveryFee = deliveryFee;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.expectedCompletionDate = expectedCompletionDate;
        this.status = status;
        this.notes = notes;
    }

    /** Business rule: Laundry Cost = Weight/Qty x Service Price; Total = Laundry Cost + fees. */
    public static BigDecimal calculateLaundryCost(BigDecimal weightQuantity, BigDecimal servicePrice) {
        return weightQuantity.multiply(servicePrice);
    }

    public static BigDecimal calculateTotalAmount(BigDecimal laundryCost, BigDecimal pickupFee,
            BigDecimal deliveryFee) {
        return laundryCost.add(pickupFee).add(deliveryFee);
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LaundryService getService() {
        return service;
    }

    public void setService(LaundryService service) {
        this.service = service;
    }

    public BigDecimal getWeightQuantity() {
        return weightQuantity;
    }

    public void setWeightQuantity(BigDecimal weightQuantity) {
        this.weightQuantity = weightQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPickupFee() {
        return pickupFee;
    }

    public void setPickupFee(BigDecimal pickupFee) {
        this.pickupFee = pickupFee;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - " + (customer != null ? customer.getFullName() : "");
    }
}
