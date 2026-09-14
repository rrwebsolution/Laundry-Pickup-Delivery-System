package service;

import dao.LaundryOrderDAO;
import dao.PaymentDAO;
import event.DataChangeEvent;
import event.DataChangeManager;
import model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();

    public List<Payment> getAll() throws SQLException {
        return paymentDAO.findAll();
    }

    public int recordPayment(Payment payment) throws ValidationException, SQLException {
        validate(payment);
        payment.setChangeAmount(Payment.calculateChange(payment.getAmountPaid(), payment.getTotalAmount()));
        payment.setPaymentStatus(Payment.determineStatus(payment.getAmountPaid(), payment.getTotalAmount()));
        int id = paymentDAO.create(payment);
        DataChangeManager.notifyListeners(DataChangeEvent.PAYMENT_CHANGED);
        return id;
    }

    public void updatePayment(Payment payment) throws ValidationException, SQLException {
        validate(payment);
        payment.setChangeAmount(Payment.calculateChange(payment.getAmountPaid(), payment.getTotalAmount()));
        payment.setPaymentStatus(Payment.determineStatus(payment.getAmountPaid(), payment.getTotalAmount()));
        if (payment.getPaymentId() <= 0) {
            throw new ValidationException("Select a payment record from the table before updating.");
        }
        paymentDAO.update(payment);
        DataChangeManager.notifyListeners(DataChangeEvent.PAYMENT_CHANGED);
    }

    public void deletePayment(int paymentId) throws SQLException {
        paymentDAO.delete(paymentId);
        DataChangeManager.notifyListeners(DataChangeEvent.PAYMENT_CHANGED);
    }

    public BigDecimal todaysRevenue() throws SQLException {
        return paymentDAO.totalRevenueForDate(LocalDate.now());
    }

    public BigDecimal monthlyRevenue(int year, int month) throws SQLException {
        return paymentDAO.totalRevenueForMonth(year, month);
    }

    private void validate(Payment payment) throws ValidationException, SQLException {
        if (payment.getOrder() == null || orderDAO.findById(payment.getOrder().getOrderId()) == null) {
            throw new ValidationException("Select an existing order for this payment.");
        }
        if (payment.getAmountPaid() == null || payment.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Amount paid cannot be negative.");
        }
        if (payment.getPaymentMethod() == null) {
            throw new ValidationException("Select a payment method.");
        }
        if (payment.getTotalAmount() == null || payment.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Total amount is invalid.");
        }
    }
}
