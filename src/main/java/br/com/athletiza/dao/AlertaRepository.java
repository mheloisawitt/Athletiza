package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Alerta;
import java.time.LocalDate;
import java.util.List;

/**
 * Consultas dos alertas da tela inicial (RF24). Implementada por AlertaDAO.
 */
public interface AlertaRepository {

    /** Tarefas de eventos e competições vencidas e não concluídas. */
    List<Alerta> tarefasAtrasadas(LocalDate hoje) throws PersistenciaException;

    /** Treinos dos últimos dias que já aconteceram e ainda não têm lista de presença. */
    List<Alerta> treinosSemPresenca(LocalDate hoje, int dias) throws PersistenciaException;

    /** Competições que começam nos próximos dias (as canceladas não contam). */
    List<Alerta> competicoesProximas(LocalDate hoje, int dias) throws PersistenciaException;
}
