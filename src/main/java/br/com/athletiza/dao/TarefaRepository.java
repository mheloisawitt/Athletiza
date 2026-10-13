package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Tarefa;
import java.util.List;

/**
 * Tarefas de eventos e de competições (RF19). Implementada por TarefaDAO.
 */
public interface TarefaRepository {

    /** A quem a tarefa pertence: um evento ou uma competição. */
    enum Dono {
        EVENTO, COMPETICAO
    }

    /** Tarefas de um evento ou competição, por prazo (as sem prazo por último). */
    List<Tarefa> listar(Dono dono, int idDono) throws PersistenciaException;

    /** Inclui (se for nova) ou altera a tarefa. */
    void salvar(Dono dono, int idDono, Tarefa tarefa) throws PersistenciaException;

    void excluir(Tarefa tarefa) throws PersistenciaException;
}
