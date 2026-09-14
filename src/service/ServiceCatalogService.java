package service;

import dao.LaundryServiceDAO;
import model.LaundryService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Business logic for managing the catalog of laundry services (Wash, Dry Cleaning, etc.). */
public class ServiceCatalogService {

    private final LaundryServiceDAO serviceDAO = new LaundryServiceDAO();

    public List<LaundryService> getAllServices() throws SQLException {
        return serviceDAO.findAll();
    }

    public int addService(LaundryService service) throws ValidationException, SQLException {
        validate(service);
        return serviceDAO.create(service);
    }

    public void updateService(LaundryService service) throws ValidationException, SQLException {
        validate(service);
        if (service.getServiceId() <= 0) {
            throw new ValidationException("Select a service from the table before updating.");
        }
        serviceDAO.update(service);
    }

    public void deleteService(int serviceId) throws SQLException {
        serviceDAO.delete(serviceId);
    }

    private void validate(LaundryService service) throws ValidationException {
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            throw new ValidationException("Service name is required.");
        }
        if (service.getPrice() == null || service.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Price cannot be negative.");
        }
        if (service.getPricingType() == null) {
            throw new ValidationException("Select a pricing type.");
        }
    }
}
