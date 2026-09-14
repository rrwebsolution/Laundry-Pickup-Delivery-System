package service;

import dao.LaundryOrderDAO;
import dao.PaymentDAO;
import dao.PickupDeliveryDAO;
import model.LaundryOrder;
import model.Payment;
import model.PickupDelivery;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Aggregates data from multiple DAOs to build the Reports section. */
public class ReportService {

    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();
    private final PickupDeliveryDAO pickupDeliveryDAO = new PickupDeliveryDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public List<LaundryOrder> dailyOrders(LocalDate date) throws SQLException {
        return orderDAO.findByDate(date);
    }

    public List<LaundryOrder> completedOrders() throws SQLException {
        return orderDAO.findAll().stream()
                .filter(o -> o.getStatus() == model.OrderStatus.DELIVERED)
                .collect(Collectors.toList());
    }

    public List<LaundryOrder> pendingOrders() throws SQLException {
        return orderDAO.findAll().stream()
                .filter(o -> o.getStatus() == model.OrderStatus.PENDING)
                .collect(Collectors.toList());
    }

    public List<PickupDelivery> pickupDeliveryRecords() throws SQLException {
        return pickupDeliveryDAO.findAll();
    }

    public List<Payment> paymentRecords() throws SQLException {
        return paymentDAO.findAll();
    }

    public BigDecimal dailyRevenue(LocalDate date) throws SQLException {
        return paymentDAO.totalRevenueForDate(date);
    }

    public BigDecimal monthlyRevenue(int year, int month) throws SQLException {
        return paymentDAO.totalRevenueForMonth(year, month);
    }
}
