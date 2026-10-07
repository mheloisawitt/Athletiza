package br.com.athletiza.controller;

import br.com.athletiza.dao.AlertaDAO;
import br.com.athletiza.dao.AtividadeDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.CompromissoDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Alerta;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Treino;
import br.com.athletiza.util.Validador;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Regras do calendário da tela inicial (CH-15 a CH-17).
 */
public class CalendarioController {

    private final AtividadeDAO atividadeDAO;
    private final CompromissoDAO compromissoDAO;
    private final AlertaDAO alertaDAO = new AlertaDAO();

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
        return atividadesDoPeriodo(mes.atDay(1), mes.atEndOfMonth());
    }

    /**
     * Atividades de um período qualquer (ex.: uma semana, RF10) agrupadas por dia,
     * cada lista ordenada por horário.
     */
    public Map<LocalDate, List<Atividade>> atividadesDoPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException {
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

    /** Dias de antecedência para avisar sobre competições. */
    static final int DIAS_AVISO_COMPETICAO = 7;
    /** Quantos dias para trás procurar treinos sem lista de presença. */
    static final int DIAS_PRESENCA_PENDENTE = 30;

    /**
     * Alertas da tela inicial (RF24): tarefas atrasadas, treinos sem presença
     * registrada e competições da próxima semana, do mais urgente ao menos urgente.
     */
    public List<Alerta> alertas(LocalDate hoje) throws PersistenciaException {
        List<Alerta> alertas = new ArrayList<>();
        alertas.addAll(alertaDAO.tarefasAtrasadas(hoje));
        alertas.addAll(alertaDAO.treinosSemPresenca(hoje, DIAS_PRESENCA_PENDENTE));
        alertas.addAll(alertaDAO.competicoesProximas(hoje, DIAS_AVISO_COMPETICAO));
        Collections.sort(alertas);
        return alertas;
    }

    /**
     * Lê a atividade completa do banco (o calendário carrega só o resumo), para
     * abrir a tela de edição a partir do calendário (RF09).
     */
    public Atividade carregarCompleta(Atividade atividade) throws PersistenciaException {
        Optional<? extends Atividade> completa;
        if (atividade instanceof Treino treino) {
            completa = new TreinoDAO().buscarPorId(treino.getId());
        } else if (atividade instanceof Competicao competicao) {
            completa = new CompeticaoDAO().buscarPorId(competicao.getId());
        } else if (atividade instanceof Evento evento) {
            completa = new EventoDAO().buscarPorId(evento.getId());
        } else {
            completa = Optional.of(atividade);
        }
        return completa.orElseThrow(() -> new PersistenciaException("Esta atividade não existe mais."));
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
