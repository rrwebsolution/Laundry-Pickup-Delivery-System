package dao;

import database.DatabaseConnection;
import model.LaundryService;
import model.PricingType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LaundryServiceDAO implements GenericDAO<LaundryService, Integer> {

    @Override
    public Integer create(LaundryService service) throws SQLException {
        String sql = "INSERT INTO laundry_services (service_name, description, price, pricing_type) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getPrice());
            stmt.setString(4, service.getPricingType().getLabel());
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
    public LaundryService findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM laundry_services WHERE service_id = ?";
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
    public List<LaundryService> findAll() throws SQLException {
        List<LaundryService> services = new ArrayList<>();
        String sql = "SELECT * FROM laundry_services ORDER BY service_id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                services.add(mapRow(rs));
            }
        }
        return services;
    }

    @Override
    public boolean update(LaundryService service) throws SQLException {
        String sql = "UPDATE laundry_services SET service_name = ?, description = ?, price = ?, pricing_type = ? "
                + "WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getPrice());
            stmt.setString(4, service.getPricingType().getLabel());
            stmt.setInt(5, service.getServiceId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM laundry_services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private LaundryService mapRow(ResultSet rs) throws SQLException {
        LaundryService service = new LaundryService();
        service.setServiceId(rs.getInt("service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setDescription(rs.getString("description"));
        service.setPrice(rs.getBigDecimal("price"));
        service.setPricingType(PricingType.fromLabel(rs.getString("pricing_type")));
        return service;
    }
}
