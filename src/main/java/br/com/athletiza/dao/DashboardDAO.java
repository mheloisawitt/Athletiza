package br.com.athletiza.dao;

import br.com.athletiza.repositorio.DashboardRepositorio;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Genero;
import br.com.athletiza.modelo.Indicador;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.PrioridadeDemanda;
import br.com.athletiza.modelo.SituacaoAtleta;
import br.com.athletiza.modelo.SituacaoDemanda;
import br.com.athletiza.modelo.Setor;
import br.com.athletiza.modelo.TipoEvento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DashboardDAO extends DAOBase implements DashboardRepositorio {
    @Override
    protected String getNomeEntidade() {
        return "os dados do dashboard";
    }

    public List<Indicador> atletasPorModalidade() throws PersistenciaException {
        return consultar("SELECT m.nome, m.genero, COUNT(*) AS atletas,"
                + " SUM(CASE WHEN am.situacao <> 'ATIVO' THEN 1 ELSE 0 END) AS indisponiveis"
                + " FROM atleta_modalidade am"
                + " JOIN atleta a ON a.id = am.atleta_id JOIN modalidade m ON m.id = am.modalidade_id"
                + " WHERE a.situacao = 'ATIVO' AND am.situacao <> 'INATIVO'"
                + " GROUP BY m.id, m.nome, m.genero", rs -> {
                    int atletas = rs.getInt("atletas");
                    int indisponiveis = rs.getInt("indisponiveis");
                    String detalhe = plural(atletas, "atleta", "atletas")
                            + (indisponiveis > 0 ? " (" + indisponiveis + " lesionado(s) ou afastado(s))" : "");
                    return new Indicador(nomeModalidade(rs.getString("nome"), rs.getString("genero")), atletas, detalhe);
                });
    }

    public List<Indicador> presencasPorModalidade(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        String sql = "SELECT m.nome, m.genero, COUNT(*) AS registros, COUNT(DISTINCT t.id) AS treinos,"
                + " SUM(CASE WHEN p.presente THEN 1 ELSE 0 END) AS presencas"
                + " FROM presenca_treino p JOIN treino t ON t.id = p.treino_id JOIN modalidade m ON m.id = t.modalidade_id"
                + periodo("t.data", inicio, fim, parametros)
                + " GROUP BY m.id, m.nome, m.genero";
        return consultar(sql, rs -> {
            int presencas = rs.getInt("presencas");
            int registros = rs.getInt("registros");
            int treinos = rs.getInt("treinos");
            String media = String.format("%.1f", presencas / (double) treinos).replace('.', ',');
            String detalhe = plural(presencas, "presença", "presenças") + " em " + plural(treinos, "treino", "treinos")
                    + " (média de " + media + " por treino, " + percentual(presencas, registros) + "% de presença)";
            return new Indicador(nomeModalidade(rs.getString("nome"), rs.getString("genero")), presencas, detalhe);
        }, parametros.toArray());
    }

    public List<Indicador> treinosPorModalidade(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        String sql = "SELECT m.nome, m.genero, COUNT(*) AS total,"
                + " SUM(CASE WHEN t.tipo = 'AMISTOSO' THEN 1 ELSE 0 END) AS amistosos"
                + " FROM treino t JOIN modalidade m ON m.id = t.modalidade_id"
                + periodo("t.data", inicio, fim, parametros)
                + " GROUP BY m.id, m.nome, m.genero";
        return consultar(sql, rs -> {
            int total = rs.getInt("total");
            int amistosos = rs.getInt("amistosos");
            String detalhe = plural(total - amistosos, "treino", "treinos") + " e " + plural(amistosos, "amistoso", "amistosos");
            return new Indicador(nomeModalidade(rs.getString("nome"), rs.getString("genero")), total, amistosos, detalhe);
        }, parametros.toArray());
    }

    public int percentualPresenca(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        String sql = "SELECT COUNT(*) AS registros, SUM(CASE WHEN p.presente THEN 1 ELSE 0 END) AS presencas"
                + " FROM presenca_treino p JOIN treino t ON t.id = p.treino_id" + periodo("t.data", inicio, fim, parametros);
        return consultar(sql, rs -> percentual(rs.getInt("presencas"), rs.getInt("registros")), parametros.toArray()).get(0);
    }

    public int contarAtletasAtivos() throws PersistenciaException {
        return consultar("SELECT COUNT(*) FROM atleta WHERE situacao = 'ATIVO'", rs -> rs.getInt(1)).get(0);
    }

    public List<DemandaAberta> demandasAbertas() throws PersistenciaException {
        return consultar("SELECT d.id, d.prazo, d.competicao_id, e.tipo_evento FROM demanda d"
                + " LEFT JOIN evento e ON e.id = d.evento_id"
                + " WHERE d.situacao IN ('ABERTA', 'EM_ANDAMENTO')", rs -> {
                    TipoEvento tipo = lerEnum(rs, "tipo_evento", TipoEvento.class);
                    rs.getInt("competicao_id");
                    Setor setor = !rs.wasNull() ? Setor.ESPORTES : tipo == null ? null : tipo.getSetor();
                    return new DemandaAberta(rs.getInt("id"), lerData(rs, "prazo"), setor);
                });
    }

    public Map<Integer, List<Integer>> responsaveisDasDemandas() throws PersistenciaException {
        Map<Integer, List<Integer>> responsaveis = new HashMap<>();
        consultar("SELECT demanda_id, membro_id FROM demanda_responsavel", rs -> {
            responsaveis.computeIfAbsent(rs.getInt("demanda_id"), id -> new ArrayList<>()).add(rs.getInt("membro_id"));
            return null;
        });
        return responsaveis;
    }

    public Map<Integer, Set<Setor>> setoresDosMembros() throws PersistenciaException {
        Map<Integer, Set<Setor>> setores = new HashMap<>();
        consultar("SELECT gmc.membro_id, c.setor FROM gestao_membro_cargo gmc"
                + " JOIN gestao g ON g.id = gmc.gestao_id JOIN cargo c ON c.id = gmc.cargo_id"
                + " WHERE g.situacao = 'EM_ANDAMENTO' AND c.setor IS NOT NULL", rs -> {
                    setores.computeIfAbsent(rs.getInt("membro_id"), id -> EnumSet.noneOf(Setor.class))
                            .add(lerEnum(rs, "setor", Setor.class));
                    return null;
                });
        return setores;
    }

    public List<RegistroPresenca> registrosDePresenca(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        return consultar("SELECT t.data, p.presente FROM presenca_treino p JOIN treino t ON t.id = p.treino_id"
                + periodo("t.data", inicio, fim, parametros),
                rs -> new RegistroPresenca(lerData(rs, "data"), rs.getBoolean("presente")), parametros.toArray());
    }

    public List<Indicador> atletasPorSituacao() throws PersistenciaException {
        return contarPorEnum("SELECT CASE WHEN a.situacao = 'INATIVO' THEN 'INATIVO' ELSE am.situacao END AS chave,"
                + " COUNT(*) AS total FROM atleta_modalidade am JOIN atleta a ON a.id = am.atleta_id"
                + " GROUP BY CASE WHEN a.situacao = 'INATIVO' THEN 'INATIVO' ELSE am.situacao END",
                SituacaoAtleta.class, "vínculo", "vínculos");
    }

    public List<Indicador> eventosPorTipo(LocalDate inicio) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        return contarPorEnum("SELECT tipo_evento AS chave, COUNT(*) AS total FROM evento"
                + periodo("data", inicio, null, parametros) + " AND situacao <> 'CANCELADO' GROUP BY tipo_evento",
                TipoEvento.class, "evento", "eventos", parametros.toArray());
    }

    public List<Indicador> demandasPorSituacao() throws PersistenciaException {
        return contarPorEnum("SELECT situacao AS chave, COUNT(*) AS total FROM demanda GROUP BY situacao",
                SituacaoDemanda.class, "demanda", "demandas");
    }

    public List<Indicador> demandasAbertasPorPrioridade() throws PersistenciaException {
        return contarPorEnum("SELECT prioridade AS chave, COUNT(*) AS total FROM demanda"
                + " WHERE situacao IN ('ABERTA', 'EM_ANDAMENTO') GROUP BY prioridade",
                PrioridadeDemanda.class, "demanda", "demandas");
    }

    public List<Podio> podios(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        return consultar("SELECT m.nome, m.genero, r.colocacao FROM resultado r"
                + " JOIN competicao c ON c.id = r.competicao_id JOIN modalidade m ON m.id = r.modalidade_id"
                + periodo("c.data", inicio, fim, parametros) + " AND r.colocacao BETWEEN 1 AND 3",
                rs -> new Podio(nomeModalidade(rs.getString("nome"), rs.getString("genero")), rs.getInt("colocacao")),
                parametros.toArray());
    }

    private <E extends Enum<E>> List<Indicador> contarPorEnum(String sql, Class<E> tipo, String singular, String plural,
            Object... parametros) throws PersistenciaException {
        return consultar(sql, rs -> {
            int total = rs.getInt("total");
            return new Indicador(lerEnum(rs, "chave", tipo).toString(), total, plural(total, singular, plural));
        }, parametros);
    }

    private static String periodo(String coluna, LocalDate inicio, LocalDate fim, List<Object> parametros) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (inicio != null) {
            where.append(" AND ").append(coluna).append(" >= ?");
            parametros.add(inicio);
        }
        if (fim != null) {
            where.append(" AND ").append(coluna).append(" <= ?");
            parametros.add(fim);
        }
        return where.toString();
    }

    private static String nomeModalidade(String nome, String genero) {
        return new Modalidade(nome, Genero.valueOf(genero)).toString();
    }

    static int percentual(int parte, int total) {
        return total == 0 ? 0 : Math.round(parte * 100f / total);
    }

    static String plural(int quantidade, String singular, String plural) {
        return quantidade + " " + (quantidade == 1 ? singular : plural);
    }
}
