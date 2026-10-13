package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;

/**
 * Acesso aos eventos e a seus responsáveis (CH-38). Implementada por EventoDAO.
 * As tarefas do evento ficam em TarefaRepository.
 */
public interface EventoRepository extends GenericDAO<Evento> {

    /** Preenche os responsáveis e as tarefas do evento. */
    void carregarDetalhes(Evento evento) throws PersistenciaException;

    void adicionarResponsavel(Evento evento, Membro membro) throws PersistenciaException;

    void removerResponsavel(Evento evento, Membro membro) throws PersistenciaException;
}
