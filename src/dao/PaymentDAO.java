package dao;

import database.DatabaseConnection;
import model.Payment;
import model.PaymentMethod;
import model.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO implements GenericDAO<Payment, Integer> {

    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();

    private static final String BASE_SELECT = "SELECT * FROM payments";

    @Override
    public Integer create(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (order_id, total_amount, amount_paid, change_amount, payment_method, "
                + "payment_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(stmt, payment);
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
    public Payment findById(Integer id) throws SQLException {
        String sql = BASE_SELECT + " WHERE payment_id = ?";
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

    public List<Payment> findByOrderId(int orderId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE order_id = ? ORDER BY payment_id";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY payment_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Payment payment) throws SQLException {
        String sql = "UPDATE payments SET order_id = ?, total_amount = ?, amount_paid = ?, change_amount = ?, "
                + "payment_method = ?, payment_status = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bind(stmt, payment);
            stmt.setInt(7, payment.getPaymentId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public BigDecimal totalRevenueForDate(LocalDate date) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE DATE(payment_date) = ? "
                + "AND payment_status IN ('Paid','Partially Paid')";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal totalRevenueForMonth(int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE YEAR(payment_date) = ? "
                + "AND MONTH(payment_date) = ? AND payment_status IN ('Paid','Partially Paid')";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private void bind(PreparedStatement stmt, Payment payment) throws SQLException {
        stmt.setInt(1, payment.getOrder().getOrderId());
        stmt.setBigDecimal(2, payment.getTotalAmount());
        stmt.setBigDecimal(3, payment.getAmountPaid());
        stmt.setBigDecimal(4, payment.getChangeAmount());
        stmt.setString(5, payment.getPaymentMethod().getLabel());
        stmt.setString(6, payment.getPaymentStatus().getLabel());
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setOrder(orderDAO.findById(rs.getInt("order_id")));
        payment.setTotalAmount(rs.getBigDecimal("total_amount"));
        payment.setAmountPaid(rs.getBigDecimal("amount_paid"));
        payment.setChangeAmount(rs.getBigDecimal("change_amount"));
        payment.setPaymentMethod(PaymentMethod.fromLabel(rs.getString("payment_method")));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) {
            payment.setPaymentDate(ts.toLocalDateTime());
        }
        payment.setPaymentStatus(PaymentStatus.fromLabel(rs.getString("payment_status")));
        return payment;
    }
}
