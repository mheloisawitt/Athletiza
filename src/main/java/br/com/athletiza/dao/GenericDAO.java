package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Entidade;
import java.util.List;

/**
 * Contrato comum de todos os DAOs do sistema (CH-06): as operações de
 * cadastro que toda entidade tem. Cada entidade tem a sua interface
 * (ex.: ModalidadeRepository), que estende esta e acrescenta o que é específico.
 *
 * @param <T> entidade gravada pelo DAO
 */
public interface GenericDAO<T extends Entidade> {

    /** Grava a entidade e preenche o id gerado pelo banco. */
    void inserir(T entidade) throws PersistenciaException;

    void atualizar(T entidade) throws PersistenciaException;

    void excluir(int id) throws PersistenciaException;

    /** Busca pelo id. Retorna null se não existir. */
    T buscarPorId(int id) throws PersistenciaException;

    List<T> listarTodos() throws PersistenciaException;
}
