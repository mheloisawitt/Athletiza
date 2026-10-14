package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Desfecho;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.SituacaoEvento;
import br.com.athletiza.model.SituacaoGestao;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.model.Treino;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Números e agrupamentos exibidos no dashboard. Reaproveita os DAOs dos módulos
 * e faz as contagens em Java, com Map e List, para funcionar igual no MySQL e no H2.
 *
 * Os gráficos com várias séries (ex.: vitórias, empates e derrotas) usam
 * Map&lt;String, int[]&gt;: a chave é a categoria e o vetor tem um valor por série.
 * Os métodos recebem a data de hoje para que os testes usem uma data fixa.
 */
public class DashboardController {

    /** Quantos meses os gráficos "por mês" mostram (o mês atual e os anteriores). */
    public static final int MESES = 12;

    private static final String[] NOMES_MESES = {"jan", "fev", "mar", "abr", "mai", "jun",
        "jul", "ago", "set", "out", "nov", "dez"};

    /** Totais dos cartões do topo do dashboard. */
    public record Totais(int atletasAtivos, int modalidades, int competicoesNoAno, int eventosNoAno) {
    }

    private final AtletaDAO atletaDAO = new AtletaDAO();
    private final ModalidadeDAO modalidadeDAO = new ModalidadeDAO();
    private final TreinoDAO treinoDAO = new TreinoDAO();
    private final CompeticaoDAO competicaoDAO = new CompeticaoDAO();
    private final EventoDAO eventoDAO = new EventoDAO();
    private final GestaoController gestaoController = new GestaoController();
    private final CalendarioController calendarioController = new CalendarioController();

    /* ===================== Visão geral ===================== */

    /** Atletas ativos, modalidades, competições e eventos do ano (cancelados não contam). */
    public Totais totais(LocalDate hoje) throws PersistenciaException {
        int ano = hoje.getYear();
        int atletasAtivos = (int) atletaDAO.listarTodos().stream()
                .filter(a -> a.getSituacao() == Situacao.ATIVO).count();
        int competicoes = (int) competicaoDAO.listarTodos().stream()
                .filter(c -> c.getSituacao() != SituacaoCompeticao.CANCELADA && c.getData().getYear() == ano).count();
        int eventos = (int) eventoDAO.listarTodos().stream()
                .filter(e -> e.getSituacao() != SituacaoEvento.CANCELADO && e.getData().getYear() == ano).count();
        return new Totais(atletasAtivos, modalidadeDAO.listarTodos().size(), competicoes, eventos);
    }

    /**
     * Treinos, competições e eventos do mês, por dia (os compromissos ficam de fora).
     * Usa o mesmo agrupamento do calendário da tela inicial.
     */
    public Map<LocalDate, List<Atividade>> linhaDoTempo(YearMonth mes) throws PersistenciaException {
        Map<LocalDate, List<Atividade>> porDia = new TreeMap<>();
        calendarioController.atividadesDoMes(mes).forEach((dia, atividades) -> {
            List<Atividade> semCompromissos = atividades.stream().filter(a -> !(a instanceof Compromisso)).toList();
            if (!semCompromissos.isEmpty()) {
                porDia.put(dia, semCompromissos);
            }
        });
        return porDia;
    }

    /* ===================== Atletas ===================== */

    /** Atletas ativos em cada modalidade (sem contar quem está inativo nela), do maior para o menor. */
    public Map<String, Integer> atletasPorModalidade() throws PersistenciaException {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (Atleta atleta : atletasAtivos()) {
            atleta.getVinculos().forEach((modalidade, situacao) -> {
                if (situacao != SituacaoAtleta.INATIVO) {
                    somar(contagem, modalidade.toString(), 1);
                }
            });
        }
        return ordenarDoMaiorParaOMenor(contagem);
    }

    /** Atletas ativos por curso, do maior para o menor. */
    public Map<String, Integer> atletasPorCurso() throws PersistenciaException {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (Atleta atleta : atletasAtivos()) {
            somar(contagem, atleta.getCurso() == null ? "Não informado" : atleta.getCurso(), 1);
        }
        return ordenarDoMaiorParaOMenor(contagem);
    }

    /** Atletas ativos por fase do curso (1ª, 2ª...), em ordem de fase. */
    public Map<String, Integer> atletasPorFase() throws PersistenciaException {
        Map<Integer, Integer> porFase = new TreeMap<>();
        int semFase = 0;
        for (Atleta atleta : atletasAtivos()) {
            if (atleta.getFase() == null) {
                semFase++;
            } else {
                somar(porFase, atleta.getFase(), 1);
            }
        }
        Map<String, Integer> resultado = new LinkedHashMap<>();
        porFase.forEach((fase, total) -> resultado.put(fase + "ª", total));
        if (semFase > 0) {
            resultado.put("N/I", semFase);
        }
        return resultado;
    }

    /** Atletas cadastrados em cada um dos últimos 12 meses. */
    public Map<String, Integer> novosCadastrosPorMes(LocalDate hoje) throws PersistenciaException {
        Map<YearMonth, Integer> porMes = mesesVazios(hoje);
        for (Atleta atleta : atletaDAO.listarTodos()) {
            if (atleta.getDataCadastro() != null) {
                somarSeExistir(porMes, YearMonth.from(atleta.getDataCadastro()), 1);
            }
        }
        return comRotulos(porMes);
    }

    /** Atletas cadastrados no ano de hoje. */
    public int novosCadastrosNoAno(LocalDate hoje) throws PersistenciaException {
        return (int) atletaDAO.listarTodos().stream()
                .filter(a -> a.getDataCadastro() != null && a.getDataCadastro().getYear() == hoje.getYear()).count();
    }

    /* ===================== Treinos ===================== */

    /** Treinos e amistosos já realizados em cada um dos últimos 12 meses: [treinos, amistosos]. */
    public Map<String, int[]> treinosPorMes(LocalDate hoje) throws PersistenciaException {
        Map<YearMonth, int[]> porMes = new LinkedHashMap<>();
        mesesVazios(hoje).keySet().forEach(mes -> porMes.put(mes, new int[2]));
        for (Treino treino : treinoDAO.listarTodos()) {
            int[] valores = porMes.get(YearMonth.from(treino.getData()));
            if (valores != null && !treino.getData().isAfter(hoje)) {
                valores[treino instanceof Amistoso ? 1 : 0]++;
            }
        }
        Map<String, int[]> resultado = new LinkedHashMap<>();
        porMes.forEach((mes, valores) -> resultado.put(rotuloMes(mes), valores));
        return resultado;
    }

    /** Treinos e amistosos realizados nos últimos 12 meses, por modalidade. */
    public Map<String, Integer> treinosPorModalidade(LocalDate hoje) throws PersistenciaException {
        LocalDate inicio = YearMonth.from(hoje).minusMonths(MESES - 1).atDay(1);
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (Treino treino : treinoDAO.listar(null, inicio, hoje)) {
            somar(contagem, treino.getModalidade().toString(), 1);
        }
        return ordenarDoMaiorParaOMenor(contagem);
    }

    /**
     * Presença média nos treinos de cada modalidade, em porcentagem (0 a 100).
     * Só entram os treinos que já têm lista de presença.
     */
    public Map<String, Integer> frequenciaPorModalidade() throws PersistenciaException {
        Map<Integer, TreinoDAO.ResumoPresenca> presencas = treinoDAO.resumirPresencas();
        Map<String, int[]> somaPorModalidade = new TreeMap<>();
        for (Treino treino : treinoDAO.listarTodos()) {
            TreinoDAO.ResumoPresenca resumo = presencas.get(treino.getId());
            if (resumo != null && resumo.registrados() > 0) {
                int[] soma = somaPorModalidade.computeIfAbsent(treino.getModalidade().toString(), m -> new int[2]);
                soma[0] += resumo.presentes();
                soma[1] += resumo.registrados();
            }
        }
        Map<String, Integer> percentuais = new LinkedHashMap<>();
        somaPorModalidade.forEach((modalidade, soma) -> percentuais.put(modalidade, Math.round(soma[0] * 100f / soma[1])));
        return ordenarDoMaiorParaOMenor(percentuais);
    }

    /** Treinos e amistosos de hoje até os próximos 6 dias, em ordem de data e horário. */
    public List<Treino> treinosDaSemana(LocalDate hoje) throws PersistenciaException {
        return treinoDAO.listar(null, hoje, hoje.plusDays(6)).stream().sorted().toList();
    }

    /* ===================== Competições ===================== */

    /** Competições futuras, em andamento e encerradas (as canceladas não entram). */
    public Map<String, Integer> competicoesPorStatus(LocalDate hoje) throws PersistenciaException {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        contagem.put("Futuras", 0);
        contagem.put("Em andamento", 0);
        contagem.put("Encerradas", 0);
        for (Competicao competicao : competicaoDAO.listarTodos()) {
            if (competicao.getSituacao() == SituacaoCompeticao.CANCELADA) {
                continue;
            }
            LocalDate termino = competicao.getDataFim() == null ? competicao.getData() : competicao.getDataFim();
            if (competicao.getSituacao() == SituacaoCompeticao.ENCERRADA || termino.isBefore(hoje)) {
                somar(contagem, "Encerradas", 1);
            } else if (competicao.getData().isAfter(hoje)) {
                somar(contagem, "Futuras", 1);
            } else {
                somar(contagem, "Em andamento", 1);
            }
        }
        return contagem;
    }

    /** Atletas inscritos em cada competição do ano, em ordem de data. */
    public Map<String, Integer> inscritosPorCompeticao(LocalDate hoje) throws PersistenciaException {
        Map<Integer, Integer> inscritos = competicaoDAO.contarInscritos();
        Map<String, Integer> resultado = new LinkedHashMap<>();
        competicaoDAO.listarTodos().stream()
                .filter(c -> c.getSituacao() != SituacaoCompeticao.CANCELADA && c.getData().getYear() == hoje.getYear())
                .sorted()
                .forEach(c -> resultado.put(c.getTitulo(), inscritos.getOrDefault(c.getId(), 0)));
        return resultado;
    }

    /** Vitórias, empates e derrotas por modalidade, pelo placar dos resultados: [vitórias, empates, derrotas]. */
    public Map<String, int[]> resultadosPorModalidade() throws PersistenciaException {
        Map<String, int[]> porModalidade = new TreeMap<>();
        for (Resultado resultado : todosOsResultados()) {
            Optional<Desfecho> desfecho = resultado.getDesfecho();
            if (desfecho.isPresent()) {
                porModalidade.computeIfAbsent(resultado.getModalidade().toString(),
                        m -> new int[Desfecho.values().length])[desfecho.get().ordinal()]++;
            }
        }
        return porModalidade;
    }

    /** Medalhas por modalidade: [ouro, prata, bronze], da modalidade com mais medalhas para a com menos. */
    public Map<String, int[]> medalhasPorModalidade() throws PersistenciaException {
        Map<String, int[]> porModalidade = new LinkedHashMap<>();
        for (Resultado resultado : todosOsResultados()) {
            if (resultado.isMedalha()) {
                porModalidade.computeIfAbsent(resultado.getModalidade().toString(), m -> new int[3])
                        [resultado.getColocacao() - 1]++;
            }
        }
        // Ranking: mais ouros primeiro; empate decide pelas pratas e depois pelos bronzes
        Comparator<int[]> ranking = Comparator.<int[]>comparingInt(m -> m[0])
                .thenComparingInt(m -> m[1]).thenComparingInt(m -> m[2]).reversed();
        Map<String, int[]> ordenado = new LinkedHashMap<>();
        porModalidade.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(ranking))
                .forEach(e -> ordenado.put(e.getKey(), e.getValue()));
        return ordenado;
    }

    /* ===================== Eventos ===================== */

    /** Eventos realizados (já aconteceram e não foram cancelados) em cada um dos últimos 12 meses. */
    public Map<String, Integer> eventosPorMes(LocalDate hoje) throws PersistenciaException {
        Map<YearMonth, Integer> porMes = mesesVazios(hoje);
        for (Evento evento : eventoDAO.listarTodos()) {
            if (evento.getSituacao() != SituacaoEvento.CANCELADO && !evento.getData().isAfter(hoje)) {
                somarSeExistir(porMes, YearMonth.from(evento.getData()), 1);
            }
        }
        return comRotulos(porMes);
    }

    /** Próximo evento planejado ou confirmado, a partir de agora. */
    public Optional<Evento> proximoEvento(LocalDateTime agora) throws PersistenciaException {
        LocalDate hoje = agora.toLocalDate();
        return eventoDAO.listarTodos().stream()
                .filter(e -> e.getSituacao() == SituacaoEvento.PLANEJADO || e.getSituacao() == SituacaoEvento.CONFIRMADO)
                .filter(e -> e.getData().isAfter(hoje) || (e.getData().equals(hoje)
                        && (e.getHorario() == null || !e.getData().atTime(e.getHorario()).isBefore(agora))))
                .sorted()
                .findFirst();
    }

    /** Eventos do ano por tipo, com os amistosos (que ficam nos treinos) como mais um tipo. */
    public Map<String, Integer> eventosPorTipo(LocalDate hoje) throws PersistenciaException {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (TipoEvento tipo : TipoEvento.values()) {
            contagem.put(tipo.getDescricao(), 0);
        }
        for (Evento evento : eventoDAO.listarTodos()) {
            if (evento.getSituacao() != SituacaoEvento.CANCELADO && evento.getData().getYear() == hoje.getYear()) {
                somar(contagem, evento.getTipoEvento().getDescricao(), 1);
            }
        }
        int amistosos = (int) treinoDAO.listar(null, LocalDate.of(hoje.getYear(), 1, 1), LocalDate.of(hoje.getYear(), 12, 31))
                .stream().filter(t -> t instanceof Amistoso).count();
        contagem.put("Amistoso", amistosos);
        contagem.values().removeIf(total -> total == 0);
        return contagem;
    }

    /* ===================== Gestão ===================== */

    /**
     * Gestão em andamento (ou a que inclui o dia de hoje), já com seus membros e cargos.
     * Vazio se nenhuma gestão estiver vigente.
     */
    public Optional<Gestao> gestaoAtual(LocalDate hoje) throws PersistenciaException {
        List<Gestao> gestoes = gestaoController.listar();
        Optional<Gestao> atual = gestoes.stream()
                .filter(g -> g.getSituacao() == SituacaoGestao.EM_ANDAMENTO && contem(g, hoje)).findFirst();
        if (atual.isEmpty()) {
            atual = gestoes.stream().filter(g -> contem(g, hoje)).findFirst();
        }
        if (atual.isPresent()) {
            gestaoController.carregarComposicao(atual.get());
        }
        return atual;
    }

    /**
     * Treinos, competições e eventos realizados no período de cada gestão: [treinos, competições, eventos].
     * Cancelados não contam. As gestões ficam em ordem de período.
     */
    public Map<String, int[]> comparativoGestoes() throws PersistenciaException {
        List<Treino> treinos = treinoDAO.listarTodos();
        List<Competicao> competicoes = competicaoDAO.listarTodos().stream()
                .filter(c -> c.getSituacao() != SituacaoCompeticao.CANCELADA).toList();
        List<Evento> eventos = eventoDAO.listarTodos().stream()
                .filter(e -> e.getSituacao() != SituacaoEvento.CANCELADO).toList();

        Map<String, int[]> comparativo = new LinkedHashMap<>();
        for (Gestao gestao : gestaoController.listar()) {
            comparativo.put(gestao.getNome(), new int[]{
                contarNoPeriodo(treinos, gestao), contarNoPeriodo(competicoes, gestao), contarNoPeriodo(eventos, gestao)});
        }
        return comparativo;
    }

    /* ===================== Auxiliares ===================== */

    /** Rótulo curto do mês, ex.: "out/26". */
    public static String rotuloMes(YearMonth mes) {
        return NOMES_MESES[mes.getMonthValue() - 1] + "/" + String.format("%02d", mes.getYear() % 100);
    }

    private List<Atleta> atletasAtivos() throws PersistenciaException {
        return atletaDAO.listarTodos().stream().filter(a -> a.getSituacao() == Situacao.ATIVO).toList();
    }

    private List<Resultado> todosOsResultados() throws PersistenciaException {
        List<Resultado> resultados = new ArrayList<>();
        competicaoDAO.listarTodos().forEach(c -> resultados.addAll(c.getResultados()));
        return resultados;
    }

    /** Os últimos 12 meses (até o mês de hoje), todos começando com zero. */
    private static Map<YearMonth, Integer> mesesVazios(LocalDate hoje) {
        Map<YearMonth, Integer> meses = new LinkedHashMap<>();
        YearMonth atual = YearMonth.from(hoje);
        for (int i = MESES - 1; i >= 0; i--) {
            meses.put(atual.minusMonths(i), 0);
        }
        return meses;
    }

    private static Map<String, Integer> comRotulos(Map<YearMonth, Integer> porMes) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        porMes.forEach((mes, total) -> resultado.put(rotuloMes(mes), total));
        return resultado;
    }

    private static <K> void somar(Map<K, Integer> contagem, K chave, int valor) {
        contagem.merge(chave, valor, Integer::sum);
    }

    /** Soma apenas se a chave já existe (ex.: meses fora do período são ignorados). */
    private static <K> void somarSeExistir(Map<K, Integer> contagem, K chave, int valor) {
        contagem.computeIfPresent(chave, (k, atual) -> atual + valor);
    }

    private static Map<String, Integer> ordenarDoMaiorParaOMenor(Map<String, Integer> contagem) {
        Map<String, Integer> ordenado = new LinkedHashMap<>();
        contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> ordenado.put(e.getKey(), e.getValue()));
        return ordenado;
    }

    private static boolean contem(Gestao gestao, LocalDate dia) {
        return !dia.isBefore(gestao.getDataInicio()) && !dia.isAfter(gestao.getDataFim());
    }

    private static int contarNoPeriodo(List<? extends Atividade> atividades, Gestao gestao) {
        return (int) atividades.stream().filter(a -> contem(gestao, a.getData())).count();
    }
}
