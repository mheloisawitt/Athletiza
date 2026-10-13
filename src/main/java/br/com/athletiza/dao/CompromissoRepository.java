package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Compromisso;
import java.time.LocalDate;
import java.util.List;

/**
 * Acesso aos compromissos do calendário (CH-16). Implementada por CompromissoDAO.
 */
public interface CompromissoRepository extends GenericDAO<Compromisso> {

    List<Compromisso> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException;
}
