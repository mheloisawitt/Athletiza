package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.SituacaoAtleta;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acesso aos dados dos atletas e de seus vínculos com modalidades (CH-24).
 * O atleta e suas modalidades são gravados na mesma transação.
 */
public class AtletaDAO extends AbstractDAO<Atleta> {

    private static final String SELECT_VINCULOS = "SELECT am.atleta_id, am.situacao, m.id AS modalidade_id,"
            + " m.nome AS modalidade_nome, m.genero AS modalidade_genero"
            + " FROM atleta_modalidade am JOIN modalidade m ON m.id = am.modalidade_id";

    /** Vínculo lido do banco: a qual atleta pertence, qual modalidade e em que situação. */
    private record Vinculo(int atletaId, Modalidade modalidade, SituacaoAtleta situacao) {
    }

    @Override
    protected String getTabela() {
        return "atleta";
    }

    @Override
    protected String getNomeEntidade() {
        return "o atleta";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "nome";
    }

    @Override
    public void inserir(Atleta atleta) throws PersistenciaException {
        int id = emTransacao("incluir", con -> {
            int novoId = inserir(con, "INSERT INTO atleta (nome, matricula, contato, data_nascimento, situacao, observacoes)"
                    + " VALUES (?, ?, ?, ?, ?, ?)",
                    atleta.getNome(), atleta.getMatricula(), atleta.getContato(), atleta.getDataNascimento(),
                    atleta.getSituacao(), atleta.getObservacoes());
            gravarVinculos(con, novoId, atleta);
            return novoId;
        });
        atleta.setId(id);
    }

    @Override
    public void atualizar(Atleta atleta) throws PersistenciaException {
        emTransacao("salvar", con -> {
            atualizar(con, "UPDATE atleta SET nome = ?, matricula = ?, contato = ?, data_nascimento = ?, situacao = ?,"
                    + " observacoes = ? WHERE id = ?",
                    atleta.getNome(), atleta.getMatricula(), atleta.getContato(), atleta.getDataNascimento(),
                    atleta.getSituacao(), atleta.getObservacoes(), atleta.getId());
            atualizar(con, "DELETE FROM atleta_modalidade WHERE atleta_id = ?", atleta.getId());
            gravarVinculos(con, atleta.getId(), atleta);
            return null;
        });
    }

    @Override
    public Optional<Atleta> buscarPorId(int id) throws PersistenciaException {
        Optional<Atleta> atleta = super.buscarPorId(id);
        if (atleta.isPresent()) {
            consultar(SELECT_VINCULOS + " WHERE am.atleta_id = ?", AtletaDAO::mapearVinculo, id)
                    .forEach(v -> atleta.get().vincularModalidade(v.modalidade(), v.situacao()));
        }
        return atleta;
    }

    /** Todos os atletas, já com suas modalidades (duas consultas, sem uma por atleta). */
    @Override
    public List<Atleta> listarTodos() throws PersistenciaException {
        List<Atleta> atletas = super.listarTodos();
        Map<Integer, Atleta> porId = new HashMap<>();
        atletas.forEach(a -> porId.put(a.getId(), a));
        for (Vinculo vinculo : consultar(SELECT_VINCULOS + " ORDER BY m.nome, m.genero", AtletaDAO::mapearVinculo)) {
            Atleta atleta = porId.get(vinculo.atletaId());
            if (atleta != null) {
                atleta.vincularModalidade(vinculo.modalidade(), vinculo.situacao());
            }
        }
        return atletas;
    }

    /** Indica se outro atleta (diferente de idIgnorado) já usa a matrícula. */
    public boolean existeMatricula(String matricula, Integer idIgnorado) throws PersistenciaException {
        return existe("SELECT COUNT(*) FROM atleta WHERE matricula = ? AND id <> ?",
                matricula, idIgnorado == null ? -1 : idIgnorado);
    }

    /**
     * Descreve os registros que impedem a exclusão do atleta (CH-45),
     * por exemplo "2 presença(s) em treinos". Lista vazia: pode excluir.
     */
    public List<String> listarVinculos(int id) throws PersistenciaException {
        List<String> vinculos = new ArrayList<>();
        int presencas = contar("SELECT COUNT(*) FROM presenca_treino WHERE atleta_id = ?", id);
        int inscricoes = contar("SELECT COUNT(*) FROM competicao_atleta WHERE atleta_id = ?", id);
        int resultados = contar("SELECT COUNT(*) FROM resultado WHERE atleta_id = ?", id);
        if (presencas > 0) {
            vinculos.add(presencas + " registro(s) de presença em treinos");
        }
        if (inscricoes > 0) {
            vinculos.add(inscricoes + " inscrição(ões) em competições");
        }
        if (resultados > 0) {
            vinculos.add(resultados + " resultado(s) em competições");
        }
        return vinculos;
    }

    private int contar(String sql, Object... parametros) throws PersistenciaException {
        return consultar(sql, rs -> rs.getInt(1), parametros).get(0);
    }

    private static void gravarVinculos(Connection con, int atletaId, Atleta atleta) throws SQLException {
        for (Map.Entry<Modalidade, SituacaoAtleta> vinculo : atleta.getVinculos().entrySet()) {
            atualizar(con, "INSERT INTO atleta_modalidade (atleta_id, modalidade_id, situacao) VALUES (?, ?, ?)",
                    atletaId, vinculo.getKey().getId(), vinculo.getValue());
        }
    }

    @Override
    protected Atleta mapear(ResultSet rs) throws SQLException {
        Atleta atleta = new Atleta(rs.getString("nome"), rs.getString("matricula"), rs.getString("contato"));
        atleta.setId(rs.getInt("id"));
        atleta.setDataNascimento(lerData(rs, "data_nascimento"));
        atleta.setSituacao(lerEnum(rs, "situacao", Situacao.class));
        atleta.setObservacoes(rs.getString("observacoes"));
        return atleta;
    }

    private static Vinculo mapearVinculo(ResultSet rs) throws SQLException {
        Modalidade modalidade = new Modalidade(rs.getString("modalidade_nome"),
                lerEnum(rs, "modalidade_genero", Genero.class));
        modalidade.setId(rs.getInt("modalidade_id"));
        return new Vinculo(rs.getInt("atleta_id"), modalidade, lerEnum(rs, "situacao", SituacaoAtleta.class));
    }
}
