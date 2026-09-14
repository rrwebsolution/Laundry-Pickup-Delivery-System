package dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Common CRUD contract implemented by every DAO. Using an interface here
 * demonstrates abstraction: GUI/service code depends on this contract, not
 * on the JDBC details of each concrete DAO.
 */
public interface GenericDAO<T, ID> {

    ID create(T entity) throws SQLException;

    T findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(ID id) throws SQLException;
}
