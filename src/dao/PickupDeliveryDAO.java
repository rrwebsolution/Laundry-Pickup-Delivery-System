package dao;

import database.DatabaseConnection;
import model.PickupDelivery;
import model.PickupDeliveryStatus;
import model.Rider;
import model.TransactionType;
import model.UserStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PickupDeliveryDAO implements GenericDAO<PickupDelivery, Integer> {

    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();

    private static final String BASE_SELECT =
            "SELECT pd.*, u.full_name AS rider_name, u.username AS rider_username, u.status AS rider_status "
            + "FROM pickup_deliveries pd "
            + "LEFT JOIN users u ON pd.assigned_rider_id = u.user_id";

    @Override
    public Integer create(PickupDelivery pd) throws SQLException {
        String sql = "INSERT INTO pickup_deliveries (order_id, type, address, assigned_rider_id, scheduled_date, "
                + "scheduled_time, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(stmt, pd);
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
    public PickupDelivery findById(Integer id) throws SQLException {
        String sql = BASE_SELECT + " WHERE pd.transaction_id = ?";
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

    public List<PickupDelivery> findByOrderId(int orderId) throws SQLException {
        List<PickupDelivery> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE pd.order_id = ? ORDER BY pd.transaction_id";
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
    public List<PickupDelivery> findAll() throws SQLException {
        List<PickupDelivery> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY pd.transaction_id DESC";
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
    public boolean update(PickupDelivery pd) throws SQLException {
        String sql = "UPDATE pickup_deliveries SET order_id = ?, type = ?, address = ?, assigned_rider_id = ?, "
                + "scheduled_date = ?, scheduled_time = ?, status = ?, notes = ? WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bind(stmt, pd);
            stmt.setInt(9, pd.getTransactionId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM pickup_deliveries WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pickup_deliveries WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private void bind(PreparedStatement stmt, PickupDelivery pd) throws SQLException {
        stmt.setInt(1, pd.getOrder().getOrderId());
        stmt.setString(2, pd.getType().getLabel());
        stmt.setString(3, pd.getAddress());
        if (pd.getAssignedRider() != null) {
            stmt.setInt(4, pd.getAssignedRider().getUserId());
        } else {
            stmt.setNull(4, java.sql.Types.INTEGER);
        }
        stmt.setDate(5, Date.valueOf(pd.getScheduledDate()));
        stmt.setTime(6, Time.valueOf(pd.getScheduledTime()));
        stmt.setString(7, pd.getStatus().getLabel());
        stmt.setString(8, pd.getNotes());
    }

    private PickupDelivery mapRow(ResultSet rs) throws SQLException {
        PickupDelivery pd = new PickupDelivery();
        pd.setTransactionId(rs.getInt("transaction_id"));
        pd.setOrder(orderDAO.findById(rs.getInt("order_id")));
        pd.setType(TransactionType.fromLabel(rs.getString("type")));
        pd.setAddress(rs.getString("address"));

        int riderId = rs.getInt("assigned_rider_id");
        if (!rs.wasNull()) {
            Rider rider = new Rider(riderId, rs.getString("rider_name"), rs.getString("rider_username"), "",
                    UserStatus.fromLabel(rs.getString("rider_status")), LocalDateTime.now());
            pd.setAssignedRider(rider);
        }

        Date scheduledDate = rs.getDate("scheduled_date");
        if (scheduledDate != null) {
            pd.setScheduledDate(scheduledDate.toLocalDate());
        }
        Time scheduledTime = rs.getTime("scheduled_time");
        if (scheduledTime != null) {
            pd.setScheduledTime(scheduledTime.toLocalTime());
        }
        pd.setStatus(PickupDeliveryStatus.fromLabel(rs.getString("status")));
        pd.setNotes(rs.getString("notes"));
        return pd;
    }
}
