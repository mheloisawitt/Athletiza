package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Entidade;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Base dos DAOs de uma entidade (CH-06). Implementa as operações comuns
 * (excluir, buscarPorId, listarTodos); os métodos auxiliares de JDBC vêm de DAOBase.
 */
public abstract class AbstractDAO<T extends Entidade> extends DAOBase implements GenericDAO<T> {

    /** Nome da tabela no banco. */
    protected abstract String getTabela();

    /** Converte a linha atual do ResultSet na entidade. */
    protected abstract T mapear(ResultSet rs) throws SQLException;

    /** Ordenação usada em listarTodos(). */
    protected String getOrdenacaoPadrao() {
        return "id";
    }

    @Override
    public void excluir(int id) throws PersistenciaException {
        executarAtualizacao("DELETE FROM " + getTabela() + " WHERE id = ?", id);
    }

    @Override
    public T buscarPorId(int id) throws PersistenciaException {
        return consultarUm("SELECT * FROM " + getTabela() + " WHERE id = ?", id);
    }

    @Override
    public List<T> listarTodos() throws PersistenciaException {
        return consultar("SELECT * FROM " + getTabela() + " ORDER BY " + getOrdenacaoPadrao());
    }

    protected List<T> consultar(String sql, Object... parametros) throws PersistenciaException {
        return consultar(sql, rs -> mapear(rs), parametros);
    }

    /** Primeiro resultado da consulta, ou null se ela não retornar nenhuma linha. */
    protected T consultarUm(String sql, Object... parametros) throws PersistenciaException {
        List<T> resultado = consultar(sql, parametros);
        return resultado.isEmpty() ? null : resultado.get(0);
    }
}
