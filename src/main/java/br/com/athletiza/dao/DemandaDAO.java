package br.com.athletiza.dao;

import br.com.athletiza.repositorio.DemandaRepositorio;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Atividade;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Demanda;
import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.PrioridadeDemanda;
import br.com.athletiza.modelo.SituacaoDemanda;
import br.com.athletiza.modelo.TipoEvento;
import br.com.athletiza.modelo.Usuario;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DemandaDAO extends AbstractDAO<Demanda> implements DemandaRepositorio {
    private static final String SELECT = "SELECT d.*, e.titulo AS evento_titulo, e.tipo_evento AS evento_tipo,"
            + " e.data AS evento_data, c.titulo AS competicao_titulo, c.data AS competicao_data,"
            + " u.nome AS solicitante_nome"
            + " FROM demanda d LEFT JOIN evento e ON e.id = d.evento_id LEFT JOIN competicao c ON c.id = d.competicao_id"
            + " LEFT JOIN usuario u ON u.id = d.solicitante_id";

    private static final String SELECT_RESPONSAVEIS = "SELECT dr.demanda_id, m.id AS membro_id, m.nome AS membro_nome,"
            + " m.matricula AS membro_matricula, m.contato AS membro_contato, m.situacao AS membro_situacao"
            + " FROM demanda_responsavel dr JOIN membro m ON m.id = dr.membro_id";

    @Override
    protected String getTabela() {
        return "demanda";
    }

    @Override
    protected String getNomeEntidade() {
        return "a demanda";
    }

    @Override
    public void inserir(Demanda demanda) throws PersistenciaException {
        int id = emTransacao("incluir", con -> {
            int novoId = inserir(con, "INSERT INTO demanda (titulo, descricao, prioridade, situacao, prazo, evento_id,"
                    + " competicao_id, solicitante_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    demanda.getTitulo(), demanda.getDescricao(), demanda.getPrioridade(), demanda.getSituacao(),
                    demanda.getPrazo(), idEvento(demanda), idCompeticao(demanda),
                    demanda.getSolicitante() == null ? null : demanda.getSolicitante().getId());
            gravarResponsaveis(con, novoId, demanda);
            return novoId;
        });
        demanda.setId(id);
    }

    @Override
    public void atualizar(Demanda demanda) throws PersistenciaException {
        emTransacao("salvar", con -> {
            atualizar(con, "UPDATE demanda SET titulo = ?, descricao = ?, prioridade = ?, situacao = ?, prazo = ?,"
                    + " evento_id = ?, competicao_id = ? WHERE id = ?",
                    demanda.getTitulo(), demanda.getDescricao(), demanda.getPrioridade(), demanda.getSituacao(),
                    demanda.getPrazo(), idEvento(demanda), idCompeticao(demanda), demanda.getId());
            atualizar(con, "DELETE FROM demanda_responsavel WHERE demanda_id = ?", demanda.getId());
            gravarResponsaveis(con, demanda.getId(), demanda);
            return null;
        });
    }

    public void atualizarSituacao(int id, SituacaoDemanda situacao) throws PersistenciaException {
        executarAtualizacao("UPDATE demanda SET situacao = ? WHERE id = ?", situacao, id);
    }

    @Override
    public Optional<Demanda> buscarPorId(int id) throws PersistenciaException {
        Optional<Demanda> demanda = consultarUm(SELECT + " WHERE d.id = ?", id);
        if (demanda.isPresent()) {
            consultar(SELECT_RESPONSAVEIS + " WHERE dr.demanda_id = ? ORDER BY m.nome",
                    rs -> MembroDAO.mapearMembro(rs, "membro_"), id)
                    .forEach(demanda.get()::adicionarResponsavel);
        }
        return demanda;
    }

    @Override
    public List<Demanda> listarTodos() throws PersistenciaException {
        List<Demanda> demandas = consultar(SELECT + " ORDER BY d.id");
        Map<Integer, Demanda> porId = new HashMap<>();
        demandas.forEach(d -> porId.put(d.getId(), d));
        consultar(SELECT_RESPONSAVEIS + " ORDER BY m.nome", rs -> {
            Demanda demanda = porId.get(rs.getInt("demanda_id"));
            if (demanda != null) {
                demanda.adicionarResponsavel(MembroDAO.mapearMembro(rs, "membro_"));
            }
            return null;
        });
        return demandas;
    }

    private static void gravarResponsaveis(Connection con, int demandaId, Demanda demanda) throws SQLException {
        for (Membro membro : demanda.getResponsaveis()) {
            atualizar(con, "INSERT INTO demanda_responsavel (demanda_id, membro_id) VALUES (?, ?)",
                    demandaId, membro.getId());
        }
    }

    private static Integer idEvento(Demanda demanda) {
        return demanda.getRelacionada() instanceof Evento evento ? evento.getId() : null;
    }

    private static Integer idCompeticao(Demanda demanda) {
        return demanda.getRelacionada() instanceof Competicao competicao ? competicao.getId() : null;
    }

    @Override
    protected Demanda mapear(ResultSet rs) throws SQLException {
        Demanda demanda = new Demanda(rs.getString("titulo"), lerEnum(rs, "prioridade", PrioridadeDemanda.class),
                lerData(rs, "prazo"));
        demanda.setId(rs.getInt("id"));
        demanda.setDescricao(rs.getString("descricao"));
        demanda.setSituacao(lerEnum(rs, "situacao", SituacaoDemanda.class));
        demanda.setRelacionada(lerRelacionada(rs));
        Integer solicitanteId = lerInteiro(rs, "solicitante_id");
        if (solicitanteId != null) {
            Usuario solicitante = new Usuario(rs.getString("solicitante_nome"), null);
            solicitante.setId(solicitanteId);
            demanda.setSolicitante(solicitante);
        }
        return demanda;
    }

    private static Atividade lerRelacionada(ResultSet rs) throws SQLException {
        Integer eventoId = lerInteiro(rs, "evento_id");
        if (eventoId != null) {
            Evento evento = new Evento(rs.getString("evento_titulo"), lerEnum(rs, "evento_tipo", TipoEvento.class),
                    lerData(rs, "evento_data"), null, null);
            evento.setId(eventoId);
            return evento;
        }
        Integer competicaoId = lerInteiro(rs, "competicao_id");
        if (competicaoId != null) {
            Competicao competicao = new Competicao(rs.getString("competicao_titulo"), lerData(rs, "competicao_data"),
                    null, null, null);
            competicao.setId(competicaoId);
            return competicao;
        }
        return null;
    }
}
