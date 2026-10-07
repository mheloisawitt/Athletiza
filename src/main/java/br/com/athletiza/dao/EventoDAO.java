package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.model.TipoEvento;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Acesso aos dados dos eventos, de seus responsáveis e de suas tarefas (CH-38).
 */
public class EventoDAO extends AbstractDAO<Evento> {

    private static final String SELECT_TAREFAS = "SELECT t.*, m.id AS membro_id, m.nome AS membro_nome,"
            + " m.matricula AS membro_matricula, m.contato AS membro_contato, m.situacao AS membro_situacao"
            + " FROM tarefa t LEFT JOIN membro m ON m.id = t.responsavel_id";

    @Override
    protected String getTabela() {
        return "evento";
    }

    @Override
    protected String getNomeEntidade() {
        return "o evento";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "data, horario";
    }

    @Override
    public void inserir(Evento evento) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO evento (titulo, tipo_evento, data, horario, local, descricao, situacao, observacoes)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                evento.getTitulo(), evento.getTipoEvento(), evento.getData(), evento.getHorario(), evento.getLocal(),
                evento.getDescricao(), evento.getSituacao(), evento.getObservacoes());
        evento.setId(id);
    }

    @Override
    public void atualizar(Evento evento) throws PersistenciaException {
        executarAtualizacao("UPDATE evento SET titulo = ?, tipo_evento = ?, data = ?, horario = ?, local = ?, descricao = ?,"
                + " situacao = ?, observacoes = ? WHERE id = ?",
                evento.getTitulo(), evento.getTipoEvento(), evento.getData(), evento.getHorario(), evento.getLocal(),
                evento.getDescricao(), evento.getSituacao(), evento.getObservacoes(), evento.getId());
    }

    /** Todos os eventos, cada um com suas tarefas (para o resumo de tarefas da consulta). */
    @Override
    public List<Evento> listarTodos() throws PersistenciaException {
        List<Evento> eventos = super.listarTodos();
        Map<Integer, Evento> porId = eventos.stream().collect(Collectors.toMap(Evento::getId, Function.identity()));
        consultar(SELECT_TAREFAS + " WHERE t.evento_id IS NOT NULL ORDER BY t.prazo", rs -> {
            porId.get(rs.getInt("evento_id")).adicionarTarefa(mapearTarefa(rs));
            return null;
        });
        return eventos;
    }

    /** Preenche os responsáveis e as tarefas do evento. */
    public void carregarDetalhes(Evento evento) throws PersistenciaException {
        consultar("SELECT m.id AS membro_id, m.nome AS membro_nome, m.matricula AS membro_matricula,"
                + " m.contato AS membro_contato, m.situacao AS membro_situacao"
                + " FROM evento_responsavel er JOIN membro m ON m.id = er.membro_id"
                + " WHERE er.evento_id = ? ORDER BY m.nome", rs -> {
                    evento.adicionarResponsavel(MembroDAO.mapearMembro(rs, "membro_"));
                    return null;
                }, evento.getId());
        consultar(SELECT_TAREFAS + " WHERE t.evento_id = ? ORDER BY t.prazo", rs -> {
            evento.adicionarTarefa(mapearTarefa(rs));
            return null;
        }, evento.getId());
    }

    public void adicionarResponsavel(Evento evento, Membro membro) throws PersistenciaException {
        executarAtualizacao("INSERT INTO evento_responsavel (evento_id, membro_id) VALUES (?, ?)",
                evento.getId(), membro.getId());
    }

    public void removerResponsavel(Evento evento, Membro membro) throws PersistenciaException {
        executarAtualizacao("DELETE FROM evento_responsavel WHERE evento_id = ? AND membro_id = ?",
                evento.getId(), membro.getId());
    }

    public void salvarTarefa(Evento evento, Tarefa tarefa) throws PersistenciaException {
        Integer responsavel = tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId();
        if (tarefa.isNova()) {
            tarefa.setId(executarInsercao("INSERT INTO tarefa (evento_id, descricao, responsavel_id, prazo, situacao)"
                    + " VALUES (?, ?, ?, ?, ?)", evento.getId(), tarefa.getDescricao(), responsavel, tarefa.getPrazo(),
                    tarefa.getSituacao()));
        } else {
            executarAtualizacao("UPDATE tarefa SET descricao = ?, responsavel_id = ?, prazo = ?, situacao = ? WHERE id = ?",
                    tarefa.getDescricao(), responsavel, tarefa.getPrazo(), tarefa.getSituacao(), tarefa.getId());
        }
    }

    public void excluirTarefa(Tarefa tarefa) throws PersistenciaException {
        executarAtualizacao("DELETE FROM tarefa WHERE id = ?", tarefa.getId());
    }

    @Override
    protected Evento mapear(ResultSet rs) throws SQLException {
        Evento evento = new Evento(rs.getString("titulo"), lerEnum(rs, "tipo_evento", TipoEvento.class),
                lerData(rs, "data"), lerHorario(rs, "horario"), rs.getString("local"));
        evento.setId(rs.getInt("id"));
        evento.setDescricao(rs.getString("descricao"));
        evento.setSituacao(lerEnum(rs, "situacao", SituacaoEvento.class));
        evento.setObservacoes(rs.getString("observacoes"));
        return evento;
    }

    private static Tarefa mapearTarefa(ResultSet rs) throws SQLException {
        Membro responsavel = lerInteiro(rs, "membro_id") == null ? null : MembroDAO.mapearMembro(rs, "membro_");
        Tarefa tarefa = new Tarefa(rs.getString("descricao"), responsavel, lerData(rs, "prazo"));
        tarefa.setId(rs.getInt("id"));
        tarefa.setSituacao(lerEnum(rs, "situacao", SituacaoTarefa.class));
        return tarefa;
    }
}
