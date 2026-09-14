package service;

import dao.CustomerDAO;
import dao.LaundryOrderDAO;
import dao.LaundryServiceDAO;
import model.LaundryOrder;
import model.LaundryService;
import model.OrderStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class OrderService {

    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final LaundryServiceDAO serviceDAO = new LaundryServiceDAO();

    public List<LaundryOrder> getAllOrders() throws SQLException {
        return orderDAO.findAll();
    }

    public List<LaundryOrder> search(String keyword, String statusFilter) throws SQLException {
        return orderDAO.search(keyword, statusFilter);
    }

    /** Computes laundry cost and total amount from weight/quantity, service price, and fees. */
    public BigDecimal computeLaundryCost(BigDecimal weightQuantity, LaundryService service) {
        if (weightQuantity == null || service == null || service.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return LaundryOrder.calculateLaundryCost(weightQuantity, service.getPrice());
    }

    public BigDecimal computeTotal(BigDecimal laundryCost, BigDecimal pickupFee, BigDecimal deliveryFee) {
        BigDecimal pf = pickupFee == null ? BigDecimal.ZERO : pickupFee;
        BigDecimal df = deliveryFee == null ? BigDecimal.ZERO : deliveryFee;
        return LaundryOrder.calculateTotalAmount(laundryCost, pf, df);
    }

    public int addOrder(LaundryOrder order) throws ValidationException, SQLException {
        validate(order);
        recalculate(order);
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        return orderDAO.create(order);
    }

    public void updateOrder(LaundryOrder order) throws ValidationException, SQLException {
        validate(order);
        recalculate(order);
        if (order.getOrderId() <= 0) {
            throw new ValidationException("Select an order from the table before updating.");
        }
        orderDAO.update(order);
    }

    public void updateStatus(int orderId, OrderStatus status) throws SQLException {
        orderDAO.updateStatus(orderId, status);
    }

    public void deleteOrder(int orderId) throws SQLException {
        orderDAO.delete(orderId);
    }

    public int countAll() throws SQLException {
        return orderDAO.countAll();
    }

    public int countByStatuses(String... statuses) throws SQLException {
        return orderDAO.countByStatuses(statuses);
    }

    private void recalculate(LaundryOrder order) {
        BigDecimal laundryCost = computeLaundryCost(order.getWeightQuantity(), order.getService());
        order.setPrice(order.getService().getPrice());
        order.setTotalAmount(computeTotal(laundryCost, order.getPickupFee(), order.getDeliveryFee()));
    }

    private void validate(LaundryOrder order) throws ValidationException, SQLException {
        if (order.getCustomer() == null || customerDAO.findById(order.getCustomer().getCustomerId()) == null) {
            throw new ValidationException("Select an existing customer for this order.");
        }
        if (order.getService() == null || serviceDAO.findById(order.getService().getServiceId()) == null) {
            throw new ValidationException("Select an existing laundry service for this order.");
        }
        if (order.getWeightQuantity() == null || order.getWeightQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Weight/quantity cannot be negative.");
        }
        if (order.getPickupFee() != null && order.getPickupFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Pickup fee cannot be negative.");
        }
        if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Delivery fee cannot be negative.");
        }
    }
}
