package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class PickupDelivery {

    private int transactionId;
    private LaundryOrder order;
    private TransactionType type;
    private String address;
    private Rider assignedRider;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private PickupDeliveryStatus status;
    private String notes;

    public PickupDelivery() {
    }

    public PickupDelivery(int transactionId, LaundryOrder order, TransactionType type, String address,
            Rider assignedRider, LocalDate scheduledDate, LocalTime scheduledTime, PickupDeliveryStatus status,
            String notes) {
        this.transactionId = transactionId;
        this.order = order;
        this.type = type;
        this.address = address;
        this.assignedRider = assignedRider;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.notes = notes;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public LaundryOrder getOrder() {
        return order;
    }

    public void setOrder(LaundryOrder order) {
        this.order = order;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Rider getAssignedRider() {
        return assignedRider;
    }

    public void setAssignedRider(Rider assignedRider) {
        this.assignedRider = assignedRider;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public PickupDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(PickupDeliveryStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
