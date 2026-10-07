package br.com.athletiza.controller;

import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.CompromissoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.util.Validador;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Regras do calendário da tela inicial (CH-15 a CH-17).
 */
public class CalendarioController {

    private final AtividadeDAO atividadeDAO;
    private final CompromissoDAO compromissoDAO;

    public CalendarioController() {
        this(new AtividadeDAO(), new CompromissoDAO());
    }

    CalendarioController(AtividadeDAO atividadeDAO, CompromissoDAO compromissoDAO) {
        this.atividadeDAO = atividadeDAO;
        this.compromissoDAO = compromissoDAO;
    }

    /**
     * Atividades do mês agrupadas por dia (CH-15). Uma competição de vários dias
     * aparece em todos eles. As listas de cada dia vêm ordenadas por horário.
     */
    public Map<LocalDate, List<Atividade>> atividadesDoMes(YearMonth mes) throws PersistenciaException {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        Map<LocalDate, List<Atividade>> porDia = new TreeMap<>();
        for (Atividade atividade : atividadeDAO.listarPorPeriodo(inicio, fim)) {
            for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
                if (atividade.ocorreEm(dia)) {
                    porDia.computeIfAbsent(dia, d -> new ArrayList<>()).add(atividade);
                }
            }
        }
        porDia.values().forEach(Collections::sort);
        return porDia;
    }

    /**
     * Próximas atividades a partir de hoje, ordenadas por data e horário (CH-17).
     * Competições em andamento também aparecem.
     *
     * @param dias quantos dias à frente considerar (ex.: 7, 15, 30)
     */
    public List<Atividade> proximasAtividades(LocalDate hoje, int dias) throws PersistenciaException {
        return atividadeDAO.listarPorPeriodo(hoje, hoje.plusDays(dias)).stream()
                .sorted()
                .toList();
    }

    /** Inclui ou altera um compromisso (CH-16). */
    public void salvarCompromisso(Compromisso compromisso) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(compromisso.getTitulo(), "Título")
                .tamanhoMaximo(compromisso.getTitulo(), 100, "Título")
                .obrigatorio(compromisso.getData(), "Data")
                .tamanhoMaximo(compromisso.getLocal(), 100, "Local")
                .tamanhoMaximo(compromisso.getObservacoes(), 500, "Observações")
                .validar();

        if (compromisso.isNova()) {
            compromissoDAO.inserir(compromisso);
        } else {
            compromissoDAO.atualizar(compromisso);
        }
    }

    public void excluirCompromisso(Compromisso compromisso) throws PersistenciaException {
        compromissoDAO.excluir(compromisso.getId());
    }
}
