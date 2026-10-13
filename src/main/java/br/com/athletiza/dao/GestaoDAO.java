package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.MembroCargo;
import br.com.athletiza.model.SituacaoGestao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acesso aos dados das gestões e de sua composição: quem ocupa cada cargo (CH-18).
 */
public class GestaoDAO extends AbstractDAO<Gestao> implements GestaoRepository {

    private final CargoDAO cargoDAO = new CargoDAO();

    @Override
    protected String getTabela() {
        return "gestao";
    }

    @Override
    protected String getNomeEntidade() {
        return "a gestão";
    }

    @Override
    protected String getOrdenacaoPadrao() {
        return "data_inicio";
    }

    @Override
    public void inserir(Gestao gestao) throws PersistenciaException {
        int id = executarInsercao("INSERT INTO gestao (nome, data_inicio, data_fim, descricao, situacao) VALUES (?, ?, ?, ?, ?)",
                gestao.getNome(), gestao.getDataInicio(), gestao.getDataFim(), gestao.getDescricao(), gestao.getSituacao());
        gestao.setId(id);
    }

    @Override
    public void atualizar(Gestao gestao) throws PersistenciaException {
        executarAtualizacao("UPDATE gestao SET nome = ?, data_inicio = ?, data_fim = ?, descricao = ?, situacao = ? WHERE id = ?",
                gestao.getNome(), gestao.getDataInicio(), gestao.getDataFim(), gestao.getDescricao(), gestao.getSituacao(),
                gestao.getId());
    }

    /**
     * Preenche a composição da gestão (membro x cargo). Os cargos vêm com o
     * cargo superior, para o organograma.
     */
    @Override
    public void carregarComposicao(Gestao gestao) throws PersistenciaException {
        Map<Integer, Cargo> cargos = new HashMap<>();
        for (Cargo cargo : cargoDAO.listarTodos()) {
            cargos.put(cargo.getId(), cargo);
        }
        List<MembroCargo> composicao = consultar("SELECT gmc.cargo_id, m.id AS membro_id, m.nome AS membro_nome,"
                + " m.matricula AS membro_matricula, m.contato AS membro_contato, m.situacao AS membro_situacao"
                + " FROM gestao_membro_cargo gmc JOIN membro m ON m.id = gmc.membro_id WHERE gmc.gestao_id = ?",
                rs -> new MembroCargo(MembroDAO.mapearMembro(rs, "membro_"), cargos.get(rs.getInt("cargo_id"))),
                gestao.getId());
        for (MembroCargo ocupacao : composicao) {
            gestao.adicionarMembro(ocupacao.getMembro(), ocupacao.getCargo());
        }
    }

    @Override
    public void adicionarMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException {
        executarAtualizacao("INSERT INTO gestao_membro_cargo (gestao_id, membro_id, cargo_id) VALUES (?, ?, ?)",
                gestao.getId(), membro.getId(), cargo.getId());
    }

    @Override
    public void removerMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException {
        executarAtualizacao("DELETE FROM gestao_membro_cargo WHERE gestao_id = ? AND membro_id = ? AND cargo_id = ?",
                gestao.getId(), membro.getId(), cargo.getId());
    }

    @Override
    public void trocarMembro(Gestao gestao, Cargo cargo, Membro atual, Membro novo) throws PersistenciaException {
        emTransacao("salvar", con -> {
            atualizar(con, "DELETE FROM gestao_membro_cargo WHERE gestao_id = ? AND membro_id = ? AND cargo_id = ?",
                    gestao.getId(), atual.getId(), cargo.getId());
            atualizar(con, "INSERT INTO gestao_membro_cargo (gestao_id, membro_id, cargo_id) VALUES (?, ?, ?)",
                    gestao.getId(), novo.getId(), cargo.getId());
            return null;
        });
    }

    @Override
    protected Gestao mapear(ResultSet rs) throws SQLException {
        Gestao gestao = new Gestao(rs.getString("nome"), lerData(rs, "data_inicio"), lerData(rs, "data_fim"));
        gestao.setId(rs.getInt("id"));
        gestao.setDescricao(rs.getString("descricao"));
        gestao.setSituacao(lerEnum(rs, "situacao", SituacaoGestao.class));
        return gestao;
    }
}
