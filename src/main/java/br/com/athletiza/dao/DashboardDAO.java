package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Indicador;
import br.com.athletiza.model.Modalidade;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas de totais que alimentam o dashboard. Os períodos vão de {@code inicio}
 * (nulo = desde o primeiro registro) até {@code fim}, inclusive.
 */
public class DashboardDAO extends DAOBase {

    /** Demanda em aberto: tarefa não concluída e quem é o responsável por ela. */
    public record DemandaAberta(Integer responsavelId, LocalDate prazo) {
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

    /** Tarefas de eventos e competições ainda não concluídas. */
    public List<DemandaAberta> demandasAbertas() throws PersistenciaException {
        return consultar("SELECT responsavel_id, prazo FROM tarefa WHERE situacao <> 'CONCLUIDA'", rs -> {
            int responsavel = rs.getInt("responsavel_id");
            return new DemandaAberta(rs.wasNull() ? null : responsavel, lerData(rs, "prazo"));
        });
    }

    /**
     * Setor (cargo) de cada membro na gestão em andamento. Quem ocupa mais de um
     * cargo fica com o de nível mais alto no organograma.
     */
    public Map<Integer, String> setorDosMembros() throws PersistenciaException {
        Map<Integer, String> setores = new HashMap<>();
        consultar("SELECT gmc.membro_id, c.nome FROM gestao_membro_cargo gmc"
                + " JOIN gestao g ON g.id = gmc.gestao_id JOIN cargo c ON c.id = gmc.cargo_id"
                + " WHERE g.situacao = 'EM_ANDAMENTO' ORDER BY g.data_inicio DESC, c.ordem, c.nome", rs -> {
                    setores.putIfAbsent(rs.getInt("membro_id"), rs.getString("nome"));
                    return null;
                });
        return setores;
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
