package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Membro;
import java.util.List;

/**
 * Acesso aos membros da atlética (CH-22). Implementada por MembroDAO.
 */
public interface MembroRepository extends GenericDAO<Membro> {

    /** Membros ativos, em ordem alfabética (para escolher ocupantes de cargos e responsáveis). */
    List<Membro> listarAtivos() throws PersistenciaException;

    /** Indica se outro membro (diferente de idIgnorado) já usa a matrícula. */
    boolean existeMatricula(String matricula, Integer idIgnorado) throws PersistenciaException;

    /** Descreve o que impede a exclusão do membro (CH-45). Lista vazia: pode excluir. */
    List<String> listarVinculos(int id) throws PersistenciaException;
}
