package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Entidade;
import java.util.List;
import java.util.Optional;

/**
 * Contrato comum de todos os DAOs do sistema (CH-06).
 *
 * @param <T> entidade gravada pelo DAO
 */
public interface GenericDAO<T extends Entidade> {

    /** Grava a entidade e preenche o id gerado pelo banco. */
    void inserir(T entidade) throws PersistenciaException;

    void atualizar(T entidade) throws PersistenciaException;

    void excluir(int id) throws PersistenciaException;

    Optional<T> buscarPorId(int id) throws PersistenciaException;

    List<T> listarTodos() throws PersistenciaException;
}
