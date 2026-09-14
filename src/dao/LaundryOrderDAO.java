package dao;

import database.DatabaseConnection;
import model.Customer;
import model.LaundryOrder;
import model.LaundryService;
import model.OrderStatus;
import model.PricingType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LaundryOrderDAO implements GenericDAO<LaundryOrder, Integer> {

    private static final String BASE_SELECT =
            "SELECT o.*, c.full_name AS customer_name, c.contact_number, c.email, c.address AS customer_address, "
            + "c.date_registered, s.service_name, s.description AS service_description, s.price AS service_price, "
            + "s.pricing_type "
            + "FROM laundry_orders o "
            + "JOIN customers c ON o.customer_id = c.customer_id "
            + "JOIN laundry_services s ON o.service_id = s.service_id";

    @Override
    public Integer create(LaundryOrder order) throws SQLException {
        String sql = "INSERT INTO laundry_orders (customer_id, service_id, weight_quantity, price, pickup_fee, "
                + "delivery_fee, total_amount, expected_completion_date, status, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindOrder(stmt, order);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return null;
    }

    @Override
    public LaundryOrder findById(Integer id) throws SQLException {
        String sql = BASE_SELECT + " WHERE o.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<LaundryOrder> findAll() throws SQLException {
        List<LaundryOrder> orders = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY o.order_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }

    public List<LaundryOrder> search(String keyword, String statusFilter) throws SQLException {
        List<LaundryOrder> orders = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (o.order_id LIKE ? OR c.full_name LIKE ?)");
        }
        if (statusFilter != null && !statusFilter.equals("All")) {
            sql.append(" AND o.status = ?");
        }
        sql.append(" ORDER BY o.order_id DESC");
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword + "%";
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
            }
            if (statusFilter != null && !statusFilter.equals("All")) {
                stmt.setString(idx++, statusFilter);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        }
        return orders;
    }

    @Override
    public boolean update(LaundryOrder order) throws SQLException {
        String sql = "UPDATE laundry_orders SET customer_id = ?, service_id = ?, weight_quantity = ?, price = ?, "
                + "pickup_fee = ?, delivery_fee = ?, total_amount = ?, expected_completion_date = ?, status = ?, "
                + "notes = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindOrder(stmt, order);
            stmt.setInt(11, order.getOrderId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE laundry_orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.getLabel());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM laundry_orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countByStatuses(String... statuses) throws SQLException {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < statuses.length; i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }
        String sql = "SELECT COUNT(*) FROM laundry_orders WHERE status IN (" + placeholders + ")";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < statuses.length; i++) {
                stmt.setString(i + 1, statuses[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM laundry_orders";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<LaundryOrder> findByDate(LocalDate date) throws SQLException {
        List<LaundryOrder> orders = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE DATE(o.order_date) = ? ORDER BY o.order_id DESC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        }
        return orders;
    }

    private void bindOrder(PreparedStatement stmt, LaundryOrder order) throws SQLException {
        stmt.setInt(1, order.getCustomer().getCustomerId());
        stmt.setInt(2, order.getService().getServiceId());
        stmt.setBigDecimal(3, order.getWeightQuantity());
        stmt.setBigDecimal(4, order.getPrice());
        stmt.setBigDecimal(5, order.getPickupFee());
        stmt.setBigDecimal(6, order.getDeliveryFee());
        stmt.setBigDecimal(7, order.getTotalAmount());
        if (order.getExpectedCompletionDate() != null) {
            stmt.setDate(8, Date.valueOf(order.getExpectedCompletionDate()));
        } else {
            stmt.setNull(8, java.sql.Types.DATE);
        }
        stmt.setString(9, order.getStatus().getLabel());
        stmt.setString(10, order.getNotes());
    }

    private LaundryOrder mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setFullName(rs.getString("customer_name"));
        customer.setContactNumber(rs.getString("contact_number"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("customer_address"));
        Timestamp registered = rs.getTimestamp("date_registered");
        if (registered != null) {
            customer.setDateRegistered(registered.toLocalDateTime());
        }

        LaundryService service = new LaundryService();
        service.setServiceId(rs.getInt("service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setDescription(rs.getString("service_description"));
        service.setPrice(rs.getBigDecimal("service_price"));
        service.setPricingType(PricingType.fromLabel(rs.getString("pricing_type")));

        LaundryOrder order = new LaundryOrder();
        order.setOrderId(rs.getInt("order_id"));
        order.setCustomer(customer);
        order.setService(service);
        order.setWeightQuantity(rs.getBigDecimal("weight_quantity"));
        order.setPrice(rs.getBigDecimal("price"));
        order.setPickupFee(rs.getBigDecimal("pickup_fee"));
        order.setDeliveryFee(rs.getBigDecimal("delivery_fee"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        Timestamp orderDate = rs.getTimestamp("order_date");
        if (orderDate != null) {
            order.setOrderDate(orderDate.toLocalDateTime());
        }
        Date expected = rs.getDate("expected_completion_date");
        if (expected != null) {
            order.setExpectedCompletionDate(expected.toLocalDate());
        }
        order.setStatus(OrderStatus.fromLabel(rs.getString("status")));
        order.setNotes(rs.getString("notes"));
        return order;
    }
}
