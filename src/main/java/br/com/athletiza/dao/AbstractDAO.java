package br.com.athletiza.dao;

import br.com.athletiza.repositorio.Repositorio;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Entidade;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDAO<T extends Entidade> extends DAOBase implements Repositorio<T> {
    protected abstract String getTabela();

    protected abstract T mapear(ResultSet rs) throws SQLException;

    protected String getOrdenacaoPadrao() {
        return "id";
    }

    @Override
    public void excluir(int id) throws PersistenciaException {
        executarAtualizacao("DELETE FROM " + getTabela() + " WHERE id = ?", id);
    }

    @Override
    public Optional<T> buscarPorId(int id) throws PersistenciaException {
        return consultarUm("SELECT * FROM " + getTabela() + " WHERE id = ?", id);
    }

    @Override
    public List<T> listarTodos() throws PersistenciaException {
        return consultar("SELECT * FROM " + getTabela() + " ORDER BY " + getOrdenacaoPadrao());
    }

    protected List<T> consultar(String sql, Object... parametros) throws PersistenciaException {
        return consultar(sql, this::mapear, parametros);
    }

    protected Optional<T> consultarUm(String sql, Object... parametros) throws PersistenciaException {
        List<T> resultado = consultar(sql, parametros);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }
}
