package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Resultado;
import java.util.List;

/**
 * Acesso às competições, inscrições e resultados (CH-28, CH-31, CH-32).
 * Implementada por CompeticaoDAO.
 */
public interface CompeticaoRepository extends GenericDAO<Competicao> {

    /** Preenche as modalidades inscritas e os atletas de cada uma. */
    void carregarInscricoes(Competicao competicao) throws PersistenciaException;

    /** Grava as inscrições (modalidades e atletas) de uma vez. */
    void salvarInscricoes(Competicao competicao) throws PersistenciaException;

    List<Resultado> listarResultados(Competicao competicao) throws PersistenciaException;

    void salvarResultado(Competicao competicao, Resultado resultado) throws PersistenciaException;

    void excluirResultado(Resultado resultado) throws PersistenciaException;
}
