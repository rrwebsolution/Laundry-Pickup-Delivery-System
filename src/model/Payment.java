package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private int paymentId;
    private LaundryOrder order;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private PaymentStatus paymentStatus;

    public Payment() {
    }

    public Payment(int paymentId, LaundryOrder order, BigDecimal totalAmount, BigDecimal amountPaid,
            BigDecimal changeAmount, PaymentMethod paymentMethod, LocalDateTime paymentDate,
            PaymentStatus paymentStatus) {
        this.paymentId = paymentId;
        this.order = order;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.changeAmount = changeAmount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.paymentStatus = paymentStatus;
    }

    /** Business rule: Change = Amount Paid - Total Amount (never negative). */
    public static BigDecimal calculateChange(BigDecimal amountPaid, BigDecimal totalAmount) {
        BigDecimal change = amountPaid.subtract(totalAmount);
        return change.signum() < 0 ? BigDecimal.ZERO : change;
    }

    /** Determines status from how much has been paid so far. */
    public static PaymentStatus determineStatus(BigDecimal amountPaid, BigDecimal totalAmount) {
        if (amountPaid.signum() <= 0) {
            return PaymentStatus.UNPAID;
        }
        if (amountPaid.compareTo(totalAmount) >= 0) {
            return PaymentStatus.PAID;
        }
        return PaymentStatus.PARTIALLY_PAID;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public LaundryOrder getOrder() {
        return order;
    }

    public void setOrder(LaundryOrder order) {
        this.order = order;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
