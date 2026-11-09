package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Indicador;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.PrioridadeDemanda;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.SituacaoDemanda;
import br.com.athletiza.model.Setor;
import br.com.athletiza.model.TipoEvento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Consultas de totais que alimentam o dashboard. Os períodos vão de {@code inicio}
 * (nulo = desde o primeiro registro) até {@code fim}, inclusive.
 */
public class DashboardDAO extends DAOBase {

    /**
     * Demanda em aberto (aberta ou em andamento), com o setor da atividade relacionada:
     * o do tipo do evento, Esportes para competições, ou nulo se não houver relação.
     */
    public record DemandaAberta(int id, LocalDate prazo, Setor setorDaAtividade) {
    }

    @Override
    protected String getNomeEntidade() {
        return "os dados do dashboard";
    }

    /** Atletas ativos por modalidade (vínculos inativos e atletas inativos não contam). */
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

    /** Presenças registradas nos treinos e amistosos de cada modalidade. */
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

    /** Treinos e amistosos já realizados por modalidade; o parcial é a quantidade de amistosos. */
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

    /** Percentual geral de presença nos treinos do período (0 se não houver lista de presença). */
    public int percentualPresenca(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        String sql = "SELECT COUNT(*) AS registros, SUM(CASE WHEN p.presente THEN 1 ELSE 0 END) AS presencas"
                + " FROM presenca_treino p JOIN treino t ON t.id = p.treino_id" + periodo("t.data", inicio, fim, parametros);
        return consultar(sql, rs -> percentual(rs.getInt("presencas"), rs.getInt("registros")), parametros.toArray()).get(0);
    }

    public int contarAtletasAtivos() throws PersistenciaException {
        return consultar("SELECT COUNT(*) FROM atleta WHERE situacao = 'ATIVO'", rs -> rs.getInt(1)).get(0);
    }

    /** Demandas abertas ou em andamento. */
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

    /** Membros responsáveis por cada demanda (id da demanda -> ids dos membros). */
    public Map<Integer, List<Integer>> responsaveisDasDemandas() throws PersistenciaException {
        Map<Integer, List<Integer>> responsaveis = new HashMap<>();
        consultar("SELECT demanda_id, membro_id FROM demanda_responsavel", rs -> {
            responsaveis.computeIfAbsent(rs.getInt("demanda_id"), id -> new ArrayList<>()).add(rs.getInt("membro_id"));
            return null;
        });
        return responsaveis;
    }

    /**
     * Setores de cada membro na gestão em andamento, pelos cargos que ocupa
     * (cargos sem setor não entram).
     */
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

    /** Uma marcação da lista de presença: data do treino e se o atleta estava presente. */
    public record RegistroPresenca(LocalDate data, boolean presente) {
    }

    /** Marcações das listas de presença dos treinos do período. */
    public List<RegistroPresenca> registrosDePresenca(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        return consultar("SELECT t.data, p.presente FROM presenca_treino p JOIN treino t ON t.id = p.treino_id"
                + periodo("t.data", inicio, fim, parametros),
                rs -> new RegistroPresenca(lerData(rs, "data"), rs.getBoolean("presente")), parametros.toArray());
    }

    /** Vínculos de atletas com modalidades por situação; atleta inativo conta como inativo em todas. */
    public List<Indicador> atletasPorSituacao() throws PersistenciaException {
        return contarPorEnum("SELECT CASE WHEN a.situacao = 'INATIVO' THEN 'INATIVO' ELSE am.situacao END AS chave,"
                + " COUNT(*) AS total FROM atleta_modalidade am JOIN atleta a ON a.id = am.atleta_id"
                + " GROUP BY CASE WHEN a.situacao = 'INATIVO' THEN 'INATIVO' ELSE am.situacao END",
                SituacaoAtleta.class, "vínculo", "vínculos");
    }

    /** Eventos não cancelados a partir de {@code inicio} (nulo = todos), inclusive os já agendados. */
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

    /** Demandas abertas ou em andamento, por prioridade. */
    public List<Indicador> demandasAbertasPorPrioridade() throws PersistenciaException {
        return contarPorEnum("SELECT prioridade AS chave, COUNT(*) AS total FROM demanda"
                + " WHERE situacao IN ('ABERTA', 'EM_ANDAMENTO') GROUP BY prioridade",
                PrioridadeDemanda.class, "demanda", "demandas");
    }

    /** Um pódio: modalidade e colocação (1, 2 ou 3). */
    public record Podio(String modalidade, int colocacao) {
    }

    /** Colocações de 1º a 3º lugar nas competições que começaram no período. */
    public List<Podio> podios(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Object> parametros = new ArrayList<>();
        return consultar("SELECT m.nome, m.genero, r.colocacao FROM resultado r"
                + " JOIN competicao c ON c.id = r.competicao_id JOIN modalidade m ON m.id = r.modalidade_id"
                + periodo("c.data", inicio, fim, parametros) + " AND r.colocacao BETWEEN 1 AND 3",
                rs -> new Podio(nomeModalidade(rs.getString("nome"), rs.getString("genero")), rs.getInt("colocacao")),
                parametros.toArray());
    }

    /** Conta por uma coluna que guarda o nome de um enum; o nome exibido é o toString do enum. */
    private <E extends Enum<E>> List<Indicador> contarPorEnum(String sql, Class<E> tipo, String singular, String plural,
            Object... parametros) throws PersistenciaException {
        return consultar(sql, rs -> {
            int total = rs.getInt("total");
            return new Indicador(lerEnum(rs, "chave", tipo).toString(), total, plural(total, singular, plural));
        }, parametros);
    }

    /** Monta o WHERE do período e acrescenta os parâmetros. */
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
