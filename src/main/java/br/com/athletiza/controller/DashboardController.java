package br.com.athletiza.controller;

import br.com.athletiza.dao.DashboardDAO;
import br.com.athletiza.dao.DashboardDAO.DemandaAberta;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.DadosDashboard;
import br.com.athletiza.model.DadosDashboard.Resumo;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Indicador;
import br.com.athletiza.model.Setor;
import java.time.LocalDate;
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
                demandas);
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
