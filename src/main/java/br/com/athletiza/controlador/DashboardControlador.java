package br.com.athletiza.controlador;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Atividade;
import br.com.athletiza.modelo.Comparativo.Serie;
import br.com.athletiza.modelo.Comparativo;
import br.com.athletiza.modelo.DadosDashboard.Distribuicao;
import br.com.athletiza.modelo.DadosDashboard.LinhaDoTempo;
import br.com.athletiza.modelo.DadosDashboard.Resumo;
import br.com.athletiza.modelo.DadosDashboard;
import br.com.athletiza.modelo.Frequencia;
import br.com.athletiza.modelo.Indicador;
import br.com.athletiza.modelo.Setor;
import br.com.athletiza.modelo.TipoAtividade;
import br.com.athletiza.repositorio.AtividadeRepositorio;
import br.com.athletiza.repositorio.DashboardRepositorio.DemandaAberta;
import br.com.athletiza.repositorio.DashboardRepositorio.Podio;
import br.com.athletiza.repositorio.DashboardRepositorio.RegistroPresenca;
import br.com.athletiza.repositorio.DashboardRepositorio;
import br.com.athletiza.repositorio.TreinoRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.visao.dashboard.PainelDashboard;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class DashboardControlador {
    public static final String SEM_SETOR = "Sem setor definido";

    static final Comparator<Frequencia> MENOS_FREQUENTE = Comparator
            .comparingInt(Frequencia::getPercentual)
            .thenComparing(Comparator.comparingInt(Frequencia::getFaltas).reversed())
            .thenComparing(Frequencia::atleta);

    private PainelDashboard visao;
    private final DashboardRepositorio repositorio;
    private final TreinoRepositorio treinoRepositorio;
    private final AtividadeRepositorio atividadeRepositorio;

    public DashboardControlador(DashboardRepositorio repositorio, TreinoRepositorio treinoRepositorio,
            AtividadeRepositorio atividadeRepositorio) {
        this.repositorio = repositorio;
        this.treinoRepositorio = treinoRepositorio;
        this.atividadeRepositorio = atividadeRepositorio;
    }

    public DashboardControlador(PainelDashboard visao, DashboardRepositorio repositorio,
            TreinoRepositorio treinoRepositorio, AtividadeRepositorio atividadeRepositorio) {
        this(repositorio, treinoRepositorio, atividadeRepositorio);
        this.visao = visao;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoCarregar(() -> carregar());
    }

    public void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            visao.apresentarDados(carregar(visao.getInicioPeriodo(hoje), hoje), hoje);
            List<Atividade> todas = new ArrayList<>(atividadeRepositorio.listarPorPeriodo(hoje,
                    hoje.plusDays(PainelDashboard.DIAS_LISTA)));
            Collections.sort(todas);
            List<Atividade> proximas = new ArrayList<>();
            for (Atividade atividade : todas) {
                if (Sessao.getAcesso().ve(atividade) && proximas.size() < PainelDashboard.MAXIMO_LISTA) {
                    proximas.add(atividade);
                }
            }
            visao.apresentarProximas(proximas);
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    public DadosDashboard carregar(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        List<Indicador> treinos = ordenar(repositorio.treinosPorModalidade(inicio, hoje));
        List<Indicador> demandas = demandasPorSetor(hoje);
        int totalTreinos = treinos.stream().mapToInt(Indicador::valor).sum();
        int amistosos = treinos.stream().mapToInt(Indicador::parcial).sum();

        List<DemandaAberta> abertas = repositorio.demandasAbertas();
        Resumo resumo = new Resumo(repositorio.contarAtletasAtivos(), totalTreinos - amistosos, amistosos,
                repositorio.percentualPresenca(inicio, hoje), abertas.size(),
                (int) abertas.stream().filter(d -> d.prazo() != null && d.prazo().isBefore(hoje)).count());
        return new DadosDashboard(resumo,
                ordenar(repositorio.atletasPorModalidade()),
                ordenar(repositorio.presencasPorModalidade(inicio, hoje)),
                treinos,
                menosFrequentes(inicio, hoje),
                demandas,
                linhaDoTempo(inicio, hoje),
                distribuicao(inicio, hoje));
    }

    public static final int DIAS_PROXIMAS = 30;

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

    public LinhaDoTempo linhaDoTempo(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        List<Atividade> realizadas = atividadeRepositorio.listarPorPeriodo(inicio == null ? LocalDate.of(1900, 1, 1) : inicio, hoje)
                .stream().filter(a -> !a.getData().isAfter(hoje) && (inicio == null || !a.getData().isBefore(inicio)))
                .toList();

        LocalDate primeira = realizadas.stream().map(Atividade::getData).min(LocalDate::compareTo)
                .orElse(inicio == null ? hoje : inicio);
        List<YearMonth> meses = meses(YearMonth.from(primeira), YearMonth.from(hoje));
        List<String> rotulos = meses.stream().map(DashboardControlador::rotulo).toList();

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
        for (RegistroPresenca registro : repositorio.registrosDePresenca(inicio, hoje)) {
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

        List<Atividade> proximas = atividadeRepositorio.listarPorPeriodo(hoje, hoje.plusDays(DIAS_PROXIMAS)).stream()
                .filter(a -> !a.getData().isBefore(hoje)).sorted().toList();
        return new LinhaDoTempo(atividades, presenca, presencasEFaltas, proximas);
    }

    public Distribuicao distribuicao(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        return new Distribuicao(repositorio.atletasPorSituacao(), ordenar(repositorio.eventosPorTipo(inicio)),
                repositorio.demandasPorSituacao(), repositorio.demandasAbertasPorPrioridade(), podiosPorModalidade(inicio, hoje));
    }

    public Comparativo podiosPorModalidade(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        Map<String, int[]> porModalidade = new TreeMap<>();
        for (Podio podio : repositorio.podios(inicio, hoje)) {
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

    static List<YearMonth> meses(YearMonth de, YearMonth ate) {
        YearMonth primeiro = de.isBefore(ate.minusMonths(MAXIMO_MESES - 1)) ? ate.minusMonths(MAXIMO_MESES - 1) : de;
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = primeiro; !mes.isAfter(ate); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    static String rotulo(YearMonth mes) {
        return MESES[mes.getMonthValue() - 1] + "/" + String.format("%02d", mes.getYear() % 100);
    }

    private static List<Integer> lista(int[] valores) {
        return java.util.Arrays.stream(valores).boxed().toList();
    }

    public List<Frequencia> menosFrequentes(LocalDate inicio, LocalDate hoje) throws PersistenciaException {
        return treinoRepositorio.frequencia(null, inicio, hoje).stream().sorted(MENOS_FREQUENTE).toList();
    }

    public List<Indicador> demandasPorSetor(LocalDate hoje) throws PersistenciaException {
        Map<Integer, Set<Setor>> setoresDoMembro = repositorio.setoresDosMembros();
        Map<Integer, List<Integer>> responsaveis = repositorio.responsaveisDasDemandas();
        Map<String, int[]> porSetor = new TreeMap<>();
        for (DemandaAberta demanda : repositorio.demandasAbertas()) {
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
