package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Entidade;
import java.util.List;
import java.util.Optional;

public interface Repositorio<T extends Entidade> {
    void inserir(T entidade) throws PersistenciaException;

    void atualizar(T entidade) throws PersistenciaException;

    void excluir(int id) throws PersistenciaException;

    Optional<T> buscarPorId(int id) throws PersistenciaException;

    List<T> listarTodos() throws PersistenciaException;
}
