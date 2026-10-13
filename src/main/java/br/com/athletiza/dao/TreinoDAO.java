package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Presenca;
import br.com.athletiza.model.PresencaEmTreino;
import br.com.athletiza.model.ResumoPresenca;
import br.com.athletiza.model.Treino;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acesso aos dados de treinos e amistosos (mesma tabela, coluna "tipo")
 * e da presença dos atletas (CH-33).
 */
public class TreinoDAO extends AbstractDAO<Treino> implements TreinoRepository {

    private static final String SELECT = "SELECT t.*, m.nome AS modalidade_nome, m.genero AS modalidade_genero,"
            + " r.id AS membro_id, r.nome AS membro_nome, r.matricula AS membro_matricula,"
            + " r.contato AS membro_contato, r.situacao AS membro_situacao"
            + " FROM treino t JOIN modalidade m ON m.id = t.modalidade_id"
            + " LEFT JOIN membro r ON r.id = t.responsavel_id";

    private final AtletaDAO atletaDAO = new AtletaDAO();

    @Override
    protected String getTabela() {
        return "treino";
    }

    @Override
    protected String getNomeEntidade() {
        return "o treino";
    }

    @Override
    public void inserir(Treino treino) throws PersistenciaException {
        Amistoso amistoso = treino instanceof Amistoso ? (Amistoso) treino : null;
        int id = executarInsercao("INSERT INTO treino (tipo, titulo, data, horario, local, modalidade_id, responsavel_id,"
                + " adversario, resultado, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                treino.getTipo(), treino.getTitulo(), treino.getData(), treino.getHorario(), treino.getLocal(),
                treino.getModalidade().getId(), idDoResponsavel(treino),
                amistoso == null ? null : amistoso.getAdversario(), amistoso == null ? null : amistoso.getResultado(),
                treino.getObservacoes());
        treino.setId(id);
    }

    @Override
    public void atualizar(Treino treino) throws PersistenciaException {
        Amistoso amistoso = treino instanceof Amistoso ? (Amistoso) treino : null;
        executarAtualizacao("UPDATE treino SET tipo = ?, titulo = ?, data = ?, horario = ?, local = ?, modalidade_id = ?,"
                + " responsavel_id = ?, adversario = ?, resultado = ?, observacoes = ? WHERE id = ?",
                treino.getTipo(), treino.getTitulo(), treino.getData(), treino.getHorario(), treino.getLocal(),
                treino.getModalidade().getId(), idDoResponsavel(treino),
                amistoso == null ? null : amistoso.getAdversario(), amistoso == null ? null : amistoso.getResultado(),
                treino.getObservacoes(), treino.getId());
    }

    @Override
    public Treino buscarPorId(int id) throws PersistenciaException {
        return consultarUm(SELECT + " WHERE t.id = ?", id);
    }

    @Override
    public List<Treino> listarTodos() throws PersistenciaException {
        return listar(null, null, null);
    }

    @Override
    public List<Treino> listar(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (modalidade != null) {
            sql.append(" AND t.modalidade_id = ?");
            parametros.add(modalidade.getId());
        }
        if (inicio != null) {
            sql.append(" AND t.data >= ?");
            parametros.add(inicio);
        }
        if (fim != null) {
            sql.append(" AND t.data <= ?");
            parametros.add(fim);
        }
        sql.append(" ORDER BY t.data, t.horario");
        return consultar(sql.toString(), parametros.toArray());
    }

    @Override
    public List<Treino> listarNoMesmoDiaELocal(Treino treino) throws PersistenciaException {
        return consultar(SELECT + " WHERE t.data = ? AND LOWER(t.local) = LOWER(?) AND t.id <> ?",
                treino.getData(), treino.getLocal(), treino.isNova() ? -1 : treino.getId());
    }

    @Override
    public Map<Integer, ResumoPresenca> resumirPresencas() throws PersistenciaException {
        Map<Integer, ResumoPresenca> resumo = new HashMap<>();
        consultar("SELECT treino_id, SUM(CASE WHEN presente THEN 1 ELSE 0 END) AS presentes, COUNT(*) AS registrados"
                + " FROM presenca_treino GROUP BY treino_id", rs -> {
                    resumo.put(rs.getInt("treino_id"), new ResumoPresenca(rs.getInt("presentes"), rs.getInt("registrados")));
                    return null;
                });
        return resumo;
    }

    @Override
    public void carregarPresencas(Treino treino) throws PersistenciaException {
        Map<Integer, Atleta> atletas = atletaDAO.listarPorId();
        consultar("SELECT atleta_id, presente FROM presenca_treino WHERE treino_id = ?", rs -> {
            treino.registrarPresenca(atletas.get(rs.getInt("atleta_id")), rs.getBoolean("presente"));
            return null;
        }, treino.getId());
    }

    @Override
    public void salvarPresencas(Treino treino) throws PersistenciaException {
        emTransacao("salvar", con -> {
            atualizar(con, "DELETE FROM presenca_treino WHERE treino_id = ?", treino.getId());
            for (Map.Entry<Atleta, Boolean> presenca : treino.getPresencas().entrySet()) {
                atualizar(con, "INSERT INTO presenca_treino (treino_id, atleta_id, presente) VALUES (?, ?, ?)",
                        treino.getId(), presenca.getKey().getId(), presenca.getValue());
            }
            return null;
        });
    }

    @Override
    public List<Frequencia> frequencia(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        StringBuilder sql = new StringBuilder("SELECT p.atleta_id, COUNT(*) AS treinos,"
                + " SUM(CASE WHEN p.presente THEN 1 ELSE 0 END) AS presencas"
                + " FROM presenca_treino p JOIN treino t ON t.id = p.treino_id WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (modalidade != null) {
            sql.append(" AND t.modalidade_id = ?");
            parametros.add(modalidade.getId());
        }
        if (inicio != null) {
            sql.append(" AND t.data >= ?");
            parametros.add(inicio);
        }
        if (fim != null) {
            sql.append(" AND t.data <= ?");
            parametros.add(fim);
        }
        sql.append(" GROUP BY p.atleta_id");
        Map<Integer, Atleta> atletas = atletaDAO.listarPorId();
        return consultar(sql.toString(), rs -> new Frequencia(atletas.get(rs.getInt("atleta_id")),
                rs.getInt("treinos"), rs.getInt("presencas")), parametros.toArray());
    }

    @Override
    public List<PresencaEmTreino> historicoDoAtleta(Atleta atleta, Modalidade modalidade, LocalDate inicio)
            throws PersistenciaException {
        StringBuilder sql = new StringBuilder(SELECT.replace("SELECT t.*,", "SELECT t.*, p.presente,"))
                .append(" JOIN presenca_treino p ON p.treino_id = t.id WHERE p.atleta_id = ?");
        List<Object> parametros = new ArrayList<>();
        parametros.add(atleta.getId());
        if (modalidade != null) {
            sql.append(" AND t.modalidade_id = ?");
            parametros.add(modalidade.getId());
        }
        if (inicio != null) {
            sql.append(" AND t.data >= ?");
            parametros.add(inicio);
        }
        sql.append(" ORDER BY t.data DESC, t.horario DESC");
        return consultar(sql.toString(), rs -> new PresencaEmTreino(mapear(rs), Presenca.de(rs.getBoolean("presente"))),
                parametros.toArray());
    }

    private static Integer idDoResponsavel(Treino treino) {
        return treino.getResponsavel() == null ? null : treino.getResponsavel().getId();
    }

    @Override
    protected Treino mapear(ResultSet rs) throws SQLException {
        Treino treino;
        if ("AMISTOSO".equals(rs.getString("tipo"))) {
            Amistoso amistoso = new Amistoso();
            amistoso.setAdversario(rs.getString("adversario"));
            amistoso.setResultado(rs.getString("resultado"));
            treino = amistoso;
        } else {
            treino = new Treino();
        }
        treino.setId(rs.getInt("id"));
        treino.setTitulo(rs.getString("titulo"));
        treino.setData(lerData(rs, "data"));
        treino.setHorario(lerHorario(rs, "horario"));
        treino.setLocal(rs.getString("local"));
        treino.setObservacoes(rs.getString("observacoes"));
        Modalidade modalidade = new Modalidade(rs.getString("modalidade_nome"), lerEnum(rs, "modalidade_genero", Genero.class));
        modalidade.setId(rs.getInt("modalidade_id"));
        treino.setModalidade(modalidade);
        if (lerInteiro(rs, "membro_id") != null) {
            treino.setResponsavel(MembroDAO.mapearMembro(rs, "membro_"));
        }
        return treino;
    }
}
