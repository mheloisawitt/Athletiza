package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atividade;
import java.time.LocalDate;
import java.util.List;

/**
 * Leitura das atividades do calendário (CH-15). Implementada por AtividadeDAO.
 */
public interface AtividadeRepository {

    /** Atividades que acontecem, ao menos em parte, entre as duas datas (inclusive). */
    List<Atividade> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException;
}
