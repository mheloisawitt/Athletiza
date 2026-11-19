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

    public Map<LocalDate, List<Atividade>> atividadesDoMes(YearMonth mes) throws PersistenciaException {
        return atividadesDoPeriodo(mes.atDay(1), mes.atEndOfMonth());
    }

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

    public List<Atividade> proximasAtividades(LocalDate hoje, int dias) throws PersistenciaException {
        return atividadeDAO.listarPorPeriodo(hoje, hoje.plusDays(dias)).stream()
                .sorted()
                .toList();
    }

    static final int DIAS_AVISO_COMPETICAO = 7;

    static final int DIAS_PRESENCA_PENDENTE = 30;

    public List<Alerta> alertas(LocalDate hoje) throws PersistenciaException {
        List<Alerta> alertas = new ArrayList<>();
        alertas.addAll(alertaDAO.tarefasAtrasadas(hoje));
        alertas.addAll(alertaDAO.treinosSemPresenca(hoje, DIAS_PRESENCA_PENDENTE));
        alertas.addAll(alertaDAO.competicoesProximas(hoje, DIAS_AVISO_COMPETICAO));
        Collections.sort(alertas);
        return alertas;
    }

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
