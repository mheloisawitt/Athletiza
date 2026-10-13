package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Arquivo;
import br.com.athletiza.model.Pasta;
import java.util.List;

/**
 * Acesso aos arquivos da galeria de registros. Implementada por ArquivoDAO.
 */
public interface ArquivoRepository extends GenericDAO<Arquivo> {

    List<Arquivo> listarPorPasta(Pasta pasta) throws PersistenciaException;
}
