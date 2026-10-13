package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atleta;
import java.util.List;

/**
 * Acesso aos dados dos atletas (CH-24). Implementada por AtletaDAO.
 */
public interface AtletaRepository extends GenericDAO<Atleta> {

    /** Indica se outro atleta (diferente de idIgnorado) já usa a matrícula. */
    boolean existeMatricula(String matricula, Integer idIgnorado) throws PersistenciaException;

    /** Descreve os registros que impedem a exclusão do atleta (CH-45). Lista vazia: pode excluir. */
    List<String> listarVinculos(int id) throws PersistenciaException;
}
