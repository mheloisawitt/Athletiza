package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tarefas de eventos e de competições (RF19). Cada tarefa pertence a um evento
 * ou a uma competição (colunas evento_id e competicao_id da tabela tarefa).
 */
public class TarefaDAO extends DAOBase implements TarefaRepository {

    private static final String SELECT = "SELECT t.*, m.id AS membro_id, m.nome AS membro_nome,"
            + " m.matricula AS membro_matricula, m.contato AS membro_contato, m.situacao AS membro_situacao"
            + " FROM tarefa t LEFT JOIN membro m ON m.id = t.responsavel_id";

    @Override
    protected String getNomeEntidade() {
        return "a tarefa";
    }

    @Override
    public List<Tarefa> listar(Dono dono, int idDono) throws PersistenciaException {
        return consultar(SELECT + " WHERE t." + coluna(dono) + " = ? ORDER BY t.prazo IS NULL, t.prazo",
                rs -> mapear(rs), idDono);
    }

    /** Tarefas de todos os eventos ou de todas as competições, agrupadas pelo id do dono. */
    Map<Integer, List<Tarefa>> listarPorDono(Dono dono) throws PersistenciaException {
        String coluna = coluna(dono);
        Map<Integer, List<Tarefa>> porDono = new HashMap<>();
        consultar(SELECT + " WHERE t." + coluna + " IS NOT NULL ORDER BY t.prazo IS NULL, t.prazo", rs -> {
            int idDono = rs.getInt(coluna);
            if (!porDono.containsKey(idDono)) {
                porDono.put(idDono, new ArrayList<>());
            }
            porDono.get(idDono).add(mapear(rs));
            return null;
        });
        return porDono;
    }

    @Override
    public void salvar(Dono dono, int idDono, Tarefa tarefa) throws PersistenciaException {
        Integer responsavel = tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId();
        if (tarefa.isNova()) {
            tarefa.setId(executarInsercao("INSERT INTO tarefa (" + coluna(dono) + ", descricao, responsavel_id, prazo,"
                    + " situacao) VALUES (?, ?, ?, ?, ?)", idDono, tarefa.getDescricao(), responsavel, tarefa.getPrazo(),
                    tarefa.getSituacao()));
        } else {
            executarAtualizacao("UPDATE tarefa SET descricao = ?, responsavel_id = ?, prazo = ?, situacao = ? WHERE id = ?",
                    tarefa.getDescricao(), responsavel, tarefa.getPrazo(), tarefa.getSituacao(), tarefa.getId());
        }
    }

    @Override
    public void excluir(Tarefa tarefa) throws PersistenciaException {
        executarAtualizacao("DELETE FROM tarefa WHERE id = ?", tarefa.getId());
    }

    /** Coluna da tabela tarefa que guarda o dono. */
    private static String coluna(Dono dono) {
        return dono == Dono.EVENTO ? "evento_id" : "competicao_id";
    }

    private static Tarefa mapear(ResultSet rs) throws SQLException {
        Membro responsavel = lerInteiro(rs, "membro_id") == null ? null : MembroDAO.mapearMembro(rs, "membro_");
        Tarefa tarefa = new Tarefa(rs.getString("descricao"), responsavel, lerData(rs, "prazo"));
        tarefa.setId(rs.getInt("id"));
        tarefa.setSituacao(lerEnum(rs, "situacao", SituacaoTarefa.class));
        return tarefa;
    }
}
