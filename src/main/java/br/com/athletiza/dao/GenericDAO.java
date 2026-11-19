package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Entidade;
import java.util.List;
import java.util.Optional;

public interface GenericDAO<T extends Entidade> {
    void inserir(T entidade) throws PersistenciaException;

    void atualizar(T entidade) throws PersistenciaException;

    void excluir(int id) throws PersistenciaException;

    Optional<T> buscarPorId(int id) throws PersistenciaException;

    List<T> listarTodos() throws PersistenciaException;
}
