package service;

import dao.LaundryOrderDAO;
import dao.PickupDeliveryDAO;
import model.PickupDelivery;

import java.sql.SQLException;
import java.util.List;

public class PickupDeliveryService {

    private final PickupDeliveryDAO pickupDeliveryDAO = new PickupDeliveryDAO();
    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();

    public List<PickupDelivery> getAll() throws SQLException {
        return pickupDeliveryDAO.findAll();
    }

    public int schedule(PickupDelivery pd) throws ValidationException, SQLException {
        validate(pd);
        return pickupDeliveryDAO.create(pd);
    }

    public void update(PickupDelivery pd) throws ValidationException, SQLException {
        validate(pd);
        if (pd.getTransactionId() <= 0) {
            throw new ValidationException("Select a pickup/delivery record from the table before updating.");
        }
        pickupDeliveryDAO.update(pd);
    }

    public void delete(int transactionId) throws SQLException {
        pickupDeliveryDAO.delete(transactionId);
    }

    public int countByStatus(String status) throws SQLException {
        return pickupDeliveryDAO.countByStatus(status);
    }

    private void validate(PickupDelivery pd) throws ValidationException, SQLException {
        if (pd.getOrder() == null || orderDAO.findById(pd.getOrder().getOrderId()) == null) {
            throw new ValidationException("Select an existing order for this pickup/delivery.");
        }
        if (pd.getType() == null) {
            throw new ValidationException("Select a type (Pickup or Delivery).");
        }
        if (pd.getAddress() == null || pd.getAddress().trim().isEmpty()) {
            throw new ValidationException("Address is required.");
        }
        if (pd.getScheduledDate() == null) {
            throw new ValidationException("Scheduled date is required.");
        }
        if (pd.getScheduledTime() == null) {
            throw new ValidationException("Scheduled time is required.");
        }
        if (pd.getStatus() == null) {
            throw new ValidationException("Select a status.");
        }
    }
}
