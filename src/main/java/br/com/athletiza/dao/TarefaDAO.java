package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Tarefas de eventos e de competições (RF19). Cada tarefa pertence a um evento
 * ou a uma competição (colunas evento_id e competicao_id da tabela tarefa).
 */
public class TarefaDAO extends DAOBase {

    /** A quem a tarefa pertence: define a coluna usada nas consultas. */
    public enum Dono {
        EVENTO("evento_id"), COMPETICAO("competicao_id");

        private final String coluna;

        Dono(String coluna) {
            this.coluna = coluna;
        }
    }

    private static final String SELECT = "SELECT t.*, m.id AS membro_id, m.nome AS membro_nome,"
            + " m.matricula AS membro_matricula, m.contato AS membro_contato, m.situacao AS membro_situacao"
            + " FROM tarefa t LEFT JOIN membro m ON m.id = t.responsavel_id";

    @Override
    protected String getNomeEntidade() {
        return "a tarefa";
    }

    /** Tarefas de um evento ou competição, por prazo (as sem prazo por último). */
    public List<Tarefa> listar(Dono dono, int idDono) throws PersistenciaException {
        return consultar(SELECT + " WHERE t." + dono.coluna + " = ? ORDER BY t.prazo IS NULL, t.prazo", TarefaDAO::mapear, idDono);
    }

    /** Tarefas de todos os eventos ou de todas as competições, agrupadas pelo id do dono. */
    public Map<Integer, List<Tarefa>> listarPorDono(Dono dono) throws PersistenciaException {
        Map<Integer, List<Tarefa>> porDono = new java.util.HashMap<>();
        consultar(SELECT + " WHERE t." + dono.coluna + " IS NOT NULL ORDER BY t.prazo IS NULL, t.prazo", rs -> {
            porDono.computeIfAbsent(rs.getInt(dono.coluna), id -> new java.util.ArrayList<>()).add(mapear(rs));
            return null;
        });
        return porDono;
    }

    public void salvar(Dono dono, int idDono, Tarefa tarefa) throws PersistenciaException {
        Integer responsavel = tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId();
        if (tarefa.isNova()) {
            tarefa.setId(executarInsercao("INSERT INTO tarefa (" + dono.coluna + ", descricao, responsavel_id, prazo, situacao)"
                    + " VALUES (?, ?, ?, ?, ?)", idDono, tarefa.getDescricao(), responsavel, tarefa.getPrazo(),
                    tarefa.getSituacao()));
        } else {
            executarAtualizacao("UPDATE tarefa SET descricao = ?, responsavel_id = ?, prazo = ?, situacao = ? WHERE id = ?",
                    tarefa.getDescricao(), responsavel, tarefa.getPrazo(), tarefa.getSituacao(), tarefa.getId());
        }
    }

    public void excluir(Tarefa tarefa) throws PersistenciaException {
        executarAtualizacao("DELETE FROM tarefa WHERE id = ?", tarefa.getId());
    }

    private static Tarefa mapear(ResultSet rs) throws SQLException {
        Membro responsavel = lerInteiro(rs, "membro_id") == null ? null : MembroDAO.mapearMembro(rs, "membro_");
        Tarefa tarefa = new Tarefa(rs.getString("descricao"), responsavel, lerData(rs, "prazo"));
        tarefa.setId(rs.getInt("id"));
        tarefa.setSituacao(lerEnum(rs, "situacao", SituacaoTarefa.class));
        return tarefa;
    }
}
