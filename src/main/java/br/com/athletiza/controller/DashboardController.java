package br.com.athletiza.controller;

import br.com.athletiza.dao.DashboardDAO;
import br.com.athletiza.dao.DashboardDAO.DemandaAberta;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.DadosDashboard;
import br.com.athletiza.model.DadosDashboard.Resumo;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Indicador;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Indicadores do dashboard. Responde às perguntas:
 * <ul>
 * <li>Qual modalidade tem mais atletas?</li>
 * <li>Qual modalidade tem mais atletas presentes nos treinos?</li>
 * <li>Qual modalidade realizou mais treinos e amistosos?</li>
 * <li>Quais atletas menos participam dos treinos?</li>
 * <li>Quais setores estão com o maior número de demandas?</li>
 * </ul>
 * Setor é o cargo que o responsável pela tarefa ocupa na gestão em andamento
 * (ex.: Diretor de Eventos); demanda é uma tarefa de evento ou competição ainda não concluída.
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
        Resumo resumo = new Resumo(dao.contarAtletasAtivos(), totalTreinos - amistosos, amistosos,
                dao.percentualPresenca(inicio, hoje),
                demandas.stream().mapToInt(Indicador::valor).sum(),
                demandas.stream().mapToInt(Indicador::parcial).sum());
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

    /** Demandas em aberto agrupadas pelo setor do responsável; o parcial é quantas já passaram do prazo. */
    public List<Indicador> demandasPorSetor(LocalDate hoje) throws PersistenciaException {
        Map<Integer, String> setorDoMembro = dao.setorDosMembros();
        Map<String, int[]> porSetor = new TreeMap<>();
        for (DemandaAberta demanda : dao.demandasAbertas()) {
            String setor = demanda.responsavelId() == null ? SEM_SETOR
                    : setorDoMembro.getOrDefault(demanda.responsavelId(), SEM_SETOR);
            int[] contagem = porSetor.computeIfAbsent(setor, s -> new int[2]);
            contagem[0]++;
            if (demanda.prazo() != null && demanda.prazo().isBefore(hoje)) {
                contagem[1]++;
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
