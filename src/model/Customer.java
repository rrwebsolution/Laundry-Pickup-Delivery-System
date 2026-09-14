package model;

import java.time.LocalDateTime;

public class Customer {

    private int customerId;
    private String fullName;
    private String contactNumber;
    private String email;
    private String address;
    private LocalDateTime dateRegistered;

    public Customer() {
    }

    public Customer(int customerId, String fullName, String contactNumber, String email, String address,
            LocalDateTime dateRegistered) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
        this.dateRegistered = dateRegistered;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(LocalDateTime dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
