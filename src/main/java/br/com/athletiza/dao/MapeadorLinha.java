package br.com.athletiza.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Converte a linha atual de um ResultSet em um objeto.
 * Interface funcional: pode ser implementada com lambda.
 */
@FunctionalInterface
public interface MapeadorLinha<T> {

    T mapear(ResultSet rs) throws SQLException;
}
