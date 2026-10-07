package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Alerta;
import br.com.athletiza.util.Validador;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas que alimentam os alertas da tela inicial (RF24).
 */
public class AlertaDAO extends DAOBase {

    @Override
    protected String getNomeEntidade() {
        return "os alertas";
    }

    /** Tarefas de eventos e competições vencidas e não concluídas. */
    public List<Alerta> tarefasAtrasadas(LocalDate hoje) throws PersistenciaException {
        return consultar("SELECT t.descricao, t.prazo, COALESCE(e.titulo, c.titulo) AS origem FROM tarefa t"
                + " LEFT JOIN evento e ON e.id = t.evento_id LEFT JOIN competicao c ON c.id = t.competicao_id"
                + " WHERE t.situacao <> 'CONCLUIDA' AND t.prazo < ? ORDER BY t.prazo", rs -> new Alerta(
                        Alerta.Tipo.TAREFA_ATRASADA, "Tarefa atrasada: " + rs.getString("descricao")
                        + " (" + rs.getString("origem") + ", venceu em "
                        + Validador.FORMATO_DATA.format(lerData(rs, "prazo")) + ")"), hoje);
    }

    /** Treinos dos últimos dias que já aconteceram e ainda não têm lista de presença. */
    public List<Alerta> treinosSemPresenca(LocalDate hoje, int dias) throws PersistenciaException {
        return consultar("SELECT t.titulo, t.data FROM treino t WHERE t.data < ? AND t.data >= ?"
                + " AND NOT EXISTS (SELECT 1 FROM presenca_treino p WHERE p.treino_id = t.id) ORDER BY t.data",
                rs -> new Alerta(Alerta.Tipo.PRESENCA_PENDENTE, "Presença não registrada: " + rs.getString("titulo")
                        + " de " + Validador.FORMATO_DATA.format(lerData(rs, "data"))), hoje, hoje.minusDays(dias));
    }

    /** Competições que começam nos próximos dias (as canceladas não contam). */
    public List<Alerta> competicoesProximas(LocalDate hoje, int dias) throws PersistenciaException {
        List<Alerta> alertas = new ArrayList<>();
        consultar("SELECT titulo, data FROM competicao WHERE data BETWEEN ? AND ? AND situacao <> 'CANCELADA'"
                + " ORDER BY data", rs -> {
                    long faltam = ChronoUnit.DAYS.between(hoje, lerData(rs, "data"));
                    String quando = faltam == 0 ? "começa hoje" : faltam == 1 ? "começa amanhã" : "começa em " + faltam + " dias";
                    alertas.add(new Alerta(Alerta.Tipo.COMPETICAO_PROXIMA, rs.getString("titulo") + " " + quando
                            + " (" + Validador.FORMATO_DATA.format(lerData(rs, "data")) + ")"));
                    return null;
                }, hoje, hoje.plusDays(dias));
        return alertas;
    }
}
