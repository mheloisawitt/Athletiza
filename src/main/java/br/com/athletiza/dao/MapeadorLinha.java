package br.com.athletiza.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface MapeadorLinha<T> {
    T mapear(ResultSet rs) throws SQLException;
}
