package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Alerta;
import br.com.athletiza.model.Validador;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Consultas que alimentam os alertas da tela inicial (RF24).
 */
public class AlertaDAO extends DAOBase implements AlertaRepository {

    @Override
    protected String getNomeEntidade() {
        return "os alertas";
    }

    @Override
    public List<Alerta> tarefasAtrasadas(LocalDate hoje) throws PersistenciaException {
        return consultar("SELECT t.descricao, t.prazo, COALESCE(e.titulo, c.titulo) AS origem FROM tarefa t"
                + " LEFT JOIN evento e ON e.id = t.evento_id LEFT JOIN competicao c ON c.id = t.competicao_id"
                + " WHERE t.situacao <> 'CONCLUIDA' AND t.prazo < ? ORDER BY t.prazo", rs -> new Alerta(
                        Alerta.Tipo.TAREFA_ATRASADA, "Tarefa atrasada: " + rs.getString("descricao")
                        + " (" + rs.getString("origem") + ", venceu em "
                        + Validador.FORMATO_DATA.format(lerData(rs, "prazo")) + ")"), hoje);
    }

    @Override
    public List<Alerta> treinosSemPresenca(LocalDate hoje, int dias) throws PersistenciaException {
        return consultar("SELECT t.titulo, t.data FROM treino t WHERE t.data < ? AND t.data >= ?"
                + " AND NOT EXISTS (SELECT 1 FROM presenca_treino p WHERE p.treino_id = t.id) ORDER BY t.data",
                rs -> new Alerta(Alerta.Tipo.PRESENCA_PENDENTE, "Presença não registrada: " + rs.getString("titulo")
                        + " de " + Validador.FORMATO_DATA.format(lerData(rs, "data"))), hoje, hoje.minusDays(dias));
    }

    @Override
    public List<Alerta> competicoesProximas(LocalDate hoje, int dias) throws PersistenciaException {
        return consultar("SELECT titulo, data FROM competicao WHERE data BETWEEN ? AND ? AND situacao <> 'CANCELADA'"
                + " ORDER BY data", rs -> {
                    LocalDate data = lerData(rs, "data");
                    long faltam = ChronoUnit.DAYS.between(hoje, data);
                    String quando;
                    if (faltam == 0) {
                        quando = "começa hoje";
                    } else if (faltam == 1) {
                        quando = "começa amanhã";
                    } else {
                        quando = "começa em " + faltam + " dias";
                    }
                    return new Alerta(Alerta.Tipo.COMPETICAO_PROXIMA, rs.getString("titulo") + " " + quando
                            + " (" + Validador.FORMATO_DATA.format(data) + ")");
                }, hoje, hoje.plusDays(dias));
    }
}
