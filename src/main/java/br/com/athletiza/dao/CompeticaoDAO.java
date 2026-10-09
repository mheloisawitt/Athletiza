package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.Tarefa;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Acesso aos dados das competições, das inscrições (competição -> modalidade -> atletas)
 * e dos resultados (CH-28, CH-31, CH-32).
 */
public class CompeticaoDAO extends AbstractDAO<Competicao> {

    private static final String SELECT_RESULTADOS = "SELECT r.*, m.nome AS modalidade_nome, m.genero AS modalidade_genero"
            + " FROM resultado r JOIN modalidade m ON m.id = r.modalidade_id";

    private final AtletaDAO atletaDAO = new AtletaDAO();
    private final TarefaDAO tarefaDAO = new TarefaDAO();

    @Override
    protected String getTabela() {
        return "competicao";
    }

    @Override
    protected String getNomeEntidade() {
        return "a competição";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "data, horario";
    }

    @Override
    public void inserir(Competicao competicao) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO competicao (titulo, data, data_fim, horario, local, situacao, observacoes)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)",
                competicao.getTitulo(), competicao.getData(), competicao.getDataFim(), competicao.getHorario(),
                competicao.getLocal(), competicao.getSituacao(), competicao.getObservacoes());
        competicao.setId(id);
    }

    @Override
    public void atualizar(Competicao competicao) throws PersistenciaException {
        executarAtualizacao("UPDATE competicao SET titulo = ?, data = ?, data_fim = ?, horario = ?, local = ?,"
                + " situacao = ?, observacoes = ? WHERE id = ?",
                competicao.getTitulo(), competicao.getData(), competicao.getDataFim(), competicao.getHorario(),
                competicao.getLocal(), competicao.getSituacao(), competicao.getObservacoes(), competicao.getId());
    }

    /** Todas as competições, cada uma com seus resultados (para a coluna de resultado da consulta). */
    @Override
    public List<Competicao> listarTodos() throws PersistenciaException {
        List<Competicao> competicoes = super.listarTodos();
        Map<Integer, Competicao> porId = competicoes.stream().collect(Collectors.toMap(Competicao::getId, Function.identity()));
        Map<Integer, Atleta> atletas = atletasPorId();
        consultar(SELECT_RESULTADOS + " ORDER BY r.colocacao IS NULL, r.colocacao", rs -> {
            Competicao competicao = porId.get(rs.getInt("competicao_id"));
            competicao.adicionarResultado(mapearResultado(rs, atletas));
            return null;
        });
        Map<Integer, List<Tarefa>> tarefas = tarefaDAO.listarPorDono(TarefaDAO.Dono.COMPETICAO);
        competicoes.forEach(c -> tarefas.getOrDefault(c.getId(), List.of()).forEach(c::adicionarTarefa));
        return competicoes;
    }

    /** Preenche as modalidades inscritas e os atletas de cada uma. */
    public void carregarInscricoes(Competicao competicao) throws PersistenciaException {
        Map<Integer, Atleta> atletas = atletasPorId();
        Map<Integer, Modalidade> modalidades = new HashMap<>();
        consultar("SELECT m.id AS modalidade_id, m.nome AS modalidade_nome, m.genero AS modalidade_genero"
                + " FROM competicao_modalidade cm JOIN modalidade m ON m.id = cm.modalidade_id"
                + " WHERE cm.competicao_id = ? ORDER BY m.nome, m.genero", rs -> {
                    Modalidade modalidade = mapearModalidade(rs);
                    modalidades.put(modalidade.getId(), modalidade);
                    competicao.adicionarModalidade(modalidade);
                    return null;
                }, competicao.getId());
        consultar("SELECT modalidade_id, atleta_id FROM competicao_atleta WHERE competicao_id = ?", rs -> {
            competicao.restaurarInscricao(modalidades.get(rs.getInt("modalidade_id")), atletas.get(rs.getInt("atleta_id")));
            return null;
        }, competicao.getId());
    }

    /**
     * Grava as inscrições da competição em uma transação. Modalidades retiradas
     * levam junto (em cascata) seus atletas e resultados.
     */
    public void salvarInscricoes(Competicao competicao) throws PersistenciaException {
        emTransacao("salvar", con -> {
            Set<Integer> gravadas = new HashSet<>(consultarIds(con, competicao.getId()));
            Set<Integer> atuais = competicao.getModalidades().stream().map(Modalidade::getId).collect(Collectors.toSet());
            for (Integer removida : gravadas) {
                if (!atuais.contains(removida)) {
                    atualizar(con, "DELETE FROM competicao_modalidade WHERE competicao_id = ? AND modalidade_id = ?",
                            competicao.getId(), removida);
                }
            }
            for (Integer nova : atuais) {
                if (!gravadas.contains(nova)) {
                    atualizar(con, "INSERT INTO competicao_modalidade (competicao_id, modalidade_id) VALUES (?, ?)",
                            competicao.getId(), nova);
                }
            }
            atualizar(con, "DELETE FROM competicao_atleta WHERE competicao_id = ?", competicao.getId());
            for (Modalidade modalidade : competicao.getModalidades()) {
                for (Atleta atleta : competicao.getAtletas(modalidade)) {
                    atualizar(con, "INSERT INTO competicao_atleta (competicao_id, modalidade_id, atleta_id) VALUES (?, ?, ?)",
                            competicao.getId(), modalidade.getId(), atleta.getId());
                }
            }
            return null;
        });
    }

    public List<Resultado> listarResultados(Competicao competicao) throws PersistenciaException {
        Map<Integer, Atleta> atletas = atletasPorId();
        return consultar(SELECT_RESULTADOS + " WHERE r.competicao_id = ? ORDER BY m.nome, m.genero, r.colocacao IS NULL, r.colocacao",
                rs -> mapearResultado(rs, atletas), competicao.getId());
    }

    public void salvarResultado(Competicao competicao, Resultado resultado) throws PersistenciaException {
        Integer atletaId = resultado.getAtleta() == null ? null : resultado.getAtleta().getId();
        if (resultado.isNova()) {
            resultado.setId(executarInsercao("INSERT INTO resultado (competicao_id, modalidade_id, atleta_id, colocacao,"
                    + " placar, observacao) VALUES (?, ?, ?, ?, ?, ?)", competicao.getId(), resultado.getModalidade().getId(),
                    atletaId, resultado.getColocacao(), resultado.getPlacar(), resultado.getObservacao()));
        } else {
            executarAtualizacao("UPDATE resultado SET modalidade_id = ?, atleta_id = ?, colocacao = ?, placar = ?,"
                    + " observacao = ? WHERE id = ?", resultado.getModalidade().getId(), atletaId,
                    resultado.getColocacao(), resultado.getPlacar(), resultado.getObservacao(), resultado.getId());
        }
    }

    public List<Tarefa> listarTarefas(Competicao competicao) throws PersistenciaException {
        return tarefaDAO.listar(TarefaDAO.Dono.COMPETICAO, competicao.getId());
    }

    public void salvarTarefa(Competicao competicao, Tarefa tarefa) throws PersistenciaException {
        tarefaDAO.salvar(TarefaDAO.Dono.COMPETICAO, competicao.getId(), tarefa);
    }

    public void excluirTarefa(Tarefa tarefa) throws PersistenciaException {
        tarefaDAO.excluir(tarefa);
    }

    public void excluirResultado(Resultado resultado) throws PersistenciaException {
        executarAtualizacao("DELETE FROM resultado WHERE id = ?", resultado.getId());
    }

    private Map<Integer, Atleta> atletasPorId() throws PersistenciaException {
        return atletaDAO.listarTodos().stream().collect(Collectors.toMap(Atleta::getId, Function.identity()));
    }

    private static List<Integer> consultarIds(Connection con, int competicaoId) throws SQLException {
        try (var ps = con.prepareStatement("SELECT modalidade_id FROM competicao_modalidade WHERE competicao_id = ?")) {
            ps.setInt(1, competicaoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> ids = new java.util.ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
                return ids;
            }
        }
    }

    @Override
    protected Competicao mapear(ResultSet rs) throws SQLException {
        Competicao competicao = new Competicao(rs.getString("titulo"), lerData(rs, "data"), lerData(rs, "data_fim"),
                lerHorario(rs, "horario"), rs.getString("local"));
        competicao.setId(rs.getInt("id"));
        competicao.setSituacao(lerEnum(rs, "situacao", SituacaoCompeticao.class));
        competicao.setObservacoes(rs.getString("observacoes"));
        return competicao;
    }

    private static Modalidade mapearModalidade(ResultSet rs) throws SQLException {
        Modalidade modalidade = new Modalidade(rs.getString("modalidade_nome"), lerEnum(rs, "modalidade_genero", Genero.class));
        modalidade.setId(rs.getInt("modalidade_id"));
        return modalidade;
    }

    private static Resultado mapearResultado(ResultSet rs, Map<Integer, Atleta> atletas) throws SQLException {
        Integer atletaId = lerInteiro(rs, "atleta_id");
        Resultado resultado = new Resultado(mapearModalidade(rs), atletaId == null ? null : atletas.get(atletaId),
                lerInteiro(rs, "colocacao"), rs.getString("placar"));
        resultado.setId(rs.getInt("id"));
        resultado.setObservacao(rs.getString("observacao"));
        return resultado;
    }
}
