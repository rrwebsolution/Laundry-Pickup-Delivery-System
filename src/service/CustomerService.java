package service;

import dao.CustomerDAO;
import event.DataChangeEvent;
import event.DataChangeManager;
import model.Customer;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerService {

    private static final Pattern CONTACT_PATTERN = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private final CustomerDAO customerDAO = new CustomerDAO();

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public List<Customer> search(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDAO.search(keyword.trim());
    }

    public int addCustomer(Customer customer) throws ValidationException, SQLException {
        validate(customer);
        int id = customerDAO.create(customer);
        DataChangeManager.notifyListeners(DataChangeEvent.CUSTOMER_CHANGED);
        return id;
    }

    public void updateCustomer(Customer customer) throws ValidationException, SQLException {
        validate(customer);
        if (customer.getCustomerId() <= 0) {
            throw new ValidationException("Select a customer from the table before updating.");
        }
        customerDAO.update(customer);
        DataChangeManager.notifyListeners(DataChangeEvent.CUSTOMER_CHANGED);
    }

    public void deleteCustomer(int customerId) throws SQLException {
        customerDAO.delete(customerId);
        DataChangeManager.notifyListeners(DataChangeEvent.CUSTOMER_CHANGED);
    }

    public int countCustomers() throws SQLException {
        return customerDAO.countAll();
    }

    private void validate(Customer customer) throws ValidationException {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (customer.getContactNumber() == null || !CONTACT_PATTERN.matcher(customer.getContactNumber().trim()).matches()) {
            throw new ValidationException("Enter a valid contact number (digits, 7-20 characters).");
        }
        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            throw new ValidationException("Address is required.");
        }
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()
                && !customer.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("Enter a valid email address, or leave it blank.");
        }
    }
}
