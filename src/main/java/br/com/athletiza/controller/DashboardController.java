package br.com.athletiza.controller;

import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.DashboardDAO;
import br.com.athletiza.dao.DashboardDAO.Podio;
import br.com.athletiza.dao.DashboardDAO.RegistroPresenca;
import br.com.athletiza.dao.DashboardDAO.DemandaAberta;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Comparativo;
import br.com.athletiza.model.Comparativo.Serie;
import br.com.athletiza.model.DadosDashboard;
import br.com.athletiza.model.DadosDashboard.Distribuicao;
import br.com.athletiza.model.DadosDashboard.LinhaDoTempo;
import br.com.athletiza.model.DadosDashboard.Resumo;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Indicador;
import br.com.athletiza.model.Setor;
import br.com.athletiza.model.TipoAtividade;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Indicadores do dashboard. Responde às perguntas:
 * <ul>
 * <li>Qual modalidade tem mais atletas?</li>
 * <li>Qual modalidade tem mais atletas presentes nos treinos?</li>
 * <li>Qual modalidade realizou mais treinos e amistosos?</li>
 * <li>Quais atletas menos participam dos treinos?</li>
 * <li>Quais setores estão com o maior número de demandas?</li>
 * </ul>
 * Demandas são as do módulo Demandas, abertas ou em andamento; o setor vem dos cargos
 * dos responsáveis na gestão em andamento (ex.: Esportes, Eventos, Financeiro).
 */
public class DashboardController {

    /** Nome usado para demandas sem responsável ou cujo responsável não tem cargo na gestão atual. */
    public static final String SEM_SETOR = "Sem setor definido";

    /** Atletas que menos participam: menor frequência primeiro; empatando, quem mais faltou; depois pelo nome. */
    static final Comparator<Frequencia> MENOS_FREQUENTE = Comparator
            .comparingInt(Frequencia::getPercentual)
            .thenComparing(Comparator.comparingInt(Frequencia::getFaltas).reversed())
            .thenComparing(Frequencia::atleta);

    private final DashboardDAO dao = new DashboardDAO();
    private final TreinoDAO treinoDAO = new TreinoDAO();
    private final AtividadeDAO atividadeDAO = new AtividadeDAO();

    /**
     * Calcula o dashboard. Treinos, amistosos e presenças consideram o período de
     * {@code inicio} (nulo = todo o histórico) até {@code hoje}; atletas e demandas
     * mostram a situação atual.
     */
    public DadosDashboard carregar(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        List<Indicador> treinos = ordenar(dao.treinosPorModalidade(inicio, hoje));
        List<Indicador> demandas = demandasPorSetor(hoje);
        int totalTreinos = treinos.stream().mapToInt(Indicador::valor).sum();
        int amistosos = treinos.stream().mapToInt(Indicador::parcial).sum();
        // Total pelas demandas (não pela soma dos setores: uma demanda pode contar em dois setores)
        List<DemandaAberta> abertas = dao.demandasAbertas();
        Resumo resumo = new Resumo(dao.contarAtletasAtivos(), totalTreinos - amistosos, amistosos,
                dao.percentualPresenca(inicio, hoje), abertas.size(),
                (int) abertas.stream().filter(d -> d.prazo() != null && d.prazo().isBefore(hoje)).count());
        return new DadosDashboard(resumo,
                ordenar(dao.atletasPorModalidade()),
                ordenar(dao.presencasPorModalidade(inicio, hoje)),
                treinos,
                menosFrequentes(inicio, hoje),
                demandas,
                linhaDoTempo(inicio, hoje),
                distribuicao(inicio, hoje));
    }

    /** Quantos dias à frente a linha do tempo mostra. */
    public static final int DIAS_PROXIMAS = 30;
    /** Máximo de meses nos gráficos por mês (os mais recentes). */
    static final int MAXIMO_MESES = 24;
    private static final String[] MESES = {"jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"};

    public static final String TREINOS = "Treinos";
    public static final String AMISTOSOS = "Amistosos";
    public static final String COMPETICOES = "Competições";
    public static final String EVENTOS = "Eventos";
    public static final String PRESENCA = "Presença";
    public static final String PRESENCAS = "Presenças";
    public static final String FALTAS = "Faltas";
    public static final String PRIMEIRO = "1º lugar";
    public static final String SEGUNDO = "2º lugar";
    public static final String TERCEIRO = "3º lugar";

    /**
     * Evolução mês a mês (do início do período até o mês atual) e as atividades dos
     * próximos 30 dias. Os meses começam no da primeira atividade realizada dentro do período.
     */
    public LinhaDoTempo linhaDoTempo(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        List<Atividade> realizadas = atividadeDAO.listarPorPeriodo(inicio == null ? LocalDate.of(1900, 1, 1) : inicio, hoje)
                .stream().filter(a -> !a.getData().isAfter(hoje) && (inicio == null || !a.getData().isBefore(inicio)))
                .toList();
        // os meses começam no primeiro com alguma atividade (e não antes do início do período)
        LocalDate primeira = realizadas.stream().map(Atividade::getData).min(LocalDate::compareTo)
                .orElse(inicio == null ? hoje : inicio);
        List<YearMonth> meses = meses(YearMonth.from(primeira), YearMonth.from(hoje));
        List<String> rotulos = meses.stream().map(DashboardController::rotulo).toList();

        Map<TipoAtividade, String> nomes = new EnumMap<>(TipoAtividade.class);
        nomes.put(TipoAtividade.TREINO, TREINOS);
        nomes.put(TipoAtividade.AMISTOSO, AMISTOSOS);
        nomes.put(TipoAtividade.COMPETICAO, COMPETICOES);
        nomes.put(TipoAtividade.EVENTO, EVENTOS);
        Map<String, int[]> porTipo = new LinkedHashMap<>();
        nomes.values().forEach(nome -> porTipo.put(nome, new int[meses.size()]));
        for (Atividade atividade : realizadas) {
            int mes = meses.indexOf(YearMonth.from(atividade.getData()));
            String nome = nomes.get(atividade.getTipo());
            if (mes >= 0 && nome != null) {
                porTipo.get(nome)[mes]++;
            }
        }
        Comparativo atividades = new Comparativo(rotulos, porTipo.entrySet().stream()
                .map(e -> new Serie(e.getKey(), lista(e.getValue()))).toList());

        int[] presencas = new int[meses.size()];
        int[] faltas = new int[meses.size()];
        for (RegistroPresenca registro : dao.registrosDePresenca(inicio, hoje)) {
            int mes = meses.indexOf(YearMonth.from(registro.data()));
            if (mes >= 0) {
                (registro.presente() ? presencas : faltas)[mes]++;
            }
        }
        List<Integer> percentuais = new ArrayList<>();
        for (int i = 0; i < meses.size(); i++) {
            int total = presencas[i] + faltas[i];
            percentuais.add(total == 0 ? null : Math.round(presencas[i] * 100f / total));
        }
        Comparativo presenca = new Comparativo(rotulos, List.of(new Serie(PRESENCA, percentuais)));
        Comparativo presencasEFaltas = new Comparativo(rotulos,
                List.of(new Serie(PRESENCAS, lista(presencas)), new Serie(FALTAS, lista(faltas))));

        List<Atividade> proximas = atividadeDAO.listarPorPeriodo(hoje, hoje.plusDays(DIAS_PROXIMAS)).stream()
                .filter(a -> !a.getData().isBefore(hoje)).sorted().toList();
        return new LinhaDoTempo(atividades, presenca, presencasEFaltas, proximas);
    }

    /** Pizzas da aba Distribuição e os pódios por modalidade. */
    public Distribuicao distribuicao(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        return new Distribuicao(dao.atletasPorSituacao(), ordenar(dao.eventosPorTipo(inicio)),
                dao.demandasPorSituacao(), dao.demandasAbertasPorPrioridade(), podiosPorModalidade(inicio, hoje));
    }

    /** 1º, 2º e 3º lugares por modalidade; modalidades com mais pódios (e mais vitórias) primeiro. */
    public Comparativo podiosPorModalidade(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        Map<String, int[]> porModalidade = new TreeMap<>();
        for (Podio podio : dao.podios(inicio, hoje)) {
            porModalidade.computeIfAbsent(podio.modalidade(), m -> new int[3])[podio.colocacao() - 1]++;
        }
        List<Map.Entry<String, int[]>> ordem = porModalidade.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, int[]>>comparingInt(e -> -(e.getValue()[0] + e.getValue()[1] + e.getValue()[2]))
                        .thenComparingInt(e -> -e.getValue()[0])
                        .thenComparingInt(e -> -e.getValue()[1]))
                .toList();
        List<Serie> series = new ArrayList<>();
        String[] nomes = {PRIMEIRO, SEGUNDO, TERCEIRO};
        for (int colocacao = 0; colocacao < 3; colocacao++) {
            int c = colocacao;
            series.add(new Serie(nomes[c], ordem.stream().map(e -> e.getValue()[c]).toList()));
        }
        return new Comparativo(ordem.stream().map(Map.Entry::getKey).toList(), series);
    }

    /** Meses de {@code de} até {@code ate}, limitados aos últimos {@link #MAXIMO_MESES}. */
    static List<YearMonth> meses(YearMonth de, YearMonth ate) {
        YearMonth primeiro = de.isBefore(ate.minusMonths(MAXIMO_MESES - 1)) ? ate.minusMonths(MAXIMO_MESES - 1) : de;
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = primeiro; !mes.isAfter(ate); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    /** Ex.: "out/26". */
    static String rotulo(YearMonth mes) {
        return MESES[mes.getMonthValue() - 1] + "/" + String.format("%02d", mes.getYear() % 100);
    }

    private static List<Integer> lista(int[] valores) {
        return java.util.Arrays.stream(valores).boxed().toList();
    }

    /** Atletas com lista de presença no período, do que menos participa para o que mais participa. */
    public List<Frequencia> menosFrequentes(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        return treinoDAO.frequencia(null, inicio, hoje).stream().sorted(MENOS_FREQUENTE).toList();
    }

    /**
     * Demandas abertas ou em andamento agrupadas por setor; o parcial é quantas já passaram do prazo.
     * O setor de uma demanda é o dos cargos dos seus responsáveis na gestão em andamento. Sem
     * responsável com setor, vale o setor da atividade relacionada (tipo do evento, ou Esportes
     * para competições). Uma demanda com responsáveis de setores diferentes conta uma vez em cada setor.
     */
    public List<Indicador> demandasPorSetor(LocalDate hoje) throws PersistenciaException {
        Map<Integer, Set<Setor>> setoresDoMembro = dao.setoresDosMembros();
        Map<Integer, List<Integer>> responsaveis = dao.responsaveisDasDemandas();
        Map<String, int[]> porSetor = new TreeMap<>();
        for (DemandaAberta demanda : dao.demandasAbertas()) {
            Set<String> setores = new TreeSet<>();
            for (int membro : responsaveis.getOrDefault(demanda.id(), List.of())) {
                setoresDoMembro.getOrDefault(membro, Set.of()).forEach(s -> setores.add(s.getDescricao()));
            }
            if (setores.isEmpty()) {
                setores.add(demanda.setorDaAtividade() == null ? SEM_SETOR : demanda.setorDaAtividade().getDescricao());
            }
            boolean atrasada = demanda.prazo() != null && demanda.prazo().isBefore(hoje);
            for (String setor : setores) {
                int[] contagem = porSetor.computeIfAbsent(setor, s -> new int[2]);
                contagem[0]++;
                if (atrasada) {
                    contagem[1]++;
                }
            }
        }
        return ordenar(porSetor.entrySet().stream()
                .map(e -> new Indicador(e.getKey(), e.getValue()[0], e.getValue()[1],
                        e.getValue()[0] + " em aberto, " + e.getValue()[1] + " atrasada(s)"))
                .toList());
    }

    private static List<Indicador> ordenar(List<Indicador> indicadores) {
        return indicadores.stream().sorted().toList();
    }
}
