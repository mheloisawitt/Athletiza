package br.com.athletiza.controlador;

import br.com.athletiza.dao.AtletaDAO;
import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.dao.TreinoDAO;
import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Alerta;
import br.com.athletiza.modelo.Atividade;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Compromisso;
import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.Treino;
import br.com.athletiza.repositorio.AlertaRepositorio;
import br.com.athletiza.repositorio.AtividadeRepositorio;
import br.com.athletiza.repositorio.CompromissoRepositorio;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.calendario.DialogoCompromisso;
import br.com.athletiza.visao.calendario.PainelCalendario;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class CalendarioControlador {
    private PainelCalendario visao;
    private final AtividadeRepositorio atividadeRepositorio;
    private final CompromissoRepositorio compromissoRepositorio;
    private final AlertaRepositorio alertaRepositorio;
    private boolean compromissoSalvo;

    public CalendarioControlador(AtividadeRepositorio atividadeRepositorio, CompromissoRepositorio compromissoRepositorio,
            AlertaRepositorio alertaRepositorio) {
        this.atividadeRepositorio = atividadeRepositorio;
        this.compromissoRepositorio = compromissoRepositorio;
        this.alertaRepositorio = alertaRepositorio;
    }

    public CalendarioControlador(PainelCalendario visao, AtividadeRepositorio atividadeRepositorio,
            CompromissoRepositorio compromissoRepositorio, AlertaRepositorio alertaRepositorio) {
        this(atividadeRepositorio, compromissoRepositorio, alertaRepositorio);
        this.visao = visao;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoCarregar(() -> carregar());
        visao.adicionarAcaoNovoCompromisso(dia -> novoCompromisso(dia));
        visao.adicionarAcaoEditarCompromisso(compromisso -> editarCompromisso(compromisso));
        visao.adicionarAcaoExcluirCompromisso(compromisso -> excluirCompromissoNaTela(compromisso));
        visao.adicionarAcaoAbrirAtividade(atividade -> abrirAtividade(atividade));
    }

    public void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            visao.apresentarAtividades(atividadesDoPeriodo(visao.getInicioPeriodo(), visao.getFimPeriodo()),
                    alertas(hoje), proximasAtividades(hoje, visao.getDiasProximos()));
        } catch (PersistenciaException e) {
            visao.apresentarAtividades(Map.of(), List.of(), List.of());
            visao.apresentarErro(e);
        }
    }

    private void novoCompromisso(LocalDate dia) {
        Compromisso compromisso = new Compromisso();
        compromisso.setData(dia == null ? LocalDate.now() : dia);
        if (abrirDialogo(compromisso)) {
            visao.mostrarDia(compromisso.getData());
        }
    }

    private void editarCompromisso(Compromisso compromisso) {
        if (abrirDialogo(compromisso)) {
            visao.mostrarDia(compromisso.getData());
        } else {
            visao.carregar();
        }
    }

    private boolean abrirDialogo(Compromisso compromisso) {
        compromissoSalvo = false;
        DialogoCompromisso dialogo = new DialogoCompromisso(visao, compromisso);
        dialogo.adicionarAcaoBtnSalvar(e -> salvarDialogo(dialogo, compromisso));
        dialogo.apresentarTela();
        return compromissoSalvo;
    }

    private void salvarDialogo(DialogoCompromisso dialogo, Compromisso compromisso) {
        try {
            compromisso.setTitulo(dialogo.getTitulo());
            compromisso.setData(dialogo.getData());
            compromisso.setHorario(dialogo.getHorario());
            compromisso.setLocal(dialogo.getLocal());
            compromisso.setObservacoes(dialogo.getObservacoes());
            salvarCompromisso(compromisso);
            compromissoSalvo = true;
            dialogo.apresentarMensagem("Compromisso salvo com sucesso.");
            dialogo.voltar();
        } catch (AthletizaException e) {
            dialogo.apresentarErro(e);
        }
    }

    private void excluirCompromissoNaTela(Compromisso compromisso) {
        if (!visao.confirmar("Deseja realmente excluir o compromisso \"" + compromisso.getTitulo() + "\"?")) {
            return;
        }
        try {
            excluirCompromisso(compromisso);
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
        visao.carregar();
    }

    private void abrirAtividade(Atividade atividade) {
        if (!Sessao.getAcesso().podeAlterar(atividade)) {
            visao.apresentarAviso("Seu perfil não permite editar esta atividade: ela é de outra área.");
            return;
        }
        try {
            Atividade completa = carregarCompleta(atividade);
            if (completa instanceof Treino treino) {
                new TreinoControlador(new TreinoDAO(), new AtletaDAO(), new ModalidadeDAO(), new MembroDAO())
                        .abrirCadastro(treino, visao.getNavegador());
            } else if (completa instanceof Competicao competicao) {
                new CompeticaoControlador(new CompeticaoDAO()).abrirCadastro(competicao, visao.getNavegador());
            } else if (completa instanceof Evento evento) {
                new EventoControlador(new EventoDAO()).abrirCadastro(evento, visao.getNavegador());
            }
        } catch (PersistenciaException e) {
            visao.apresentarErro(e);
        }
    }

    public Map<LocalDate, List<Atividade>> atividadesDoMes(YearMonth mes) throws PersistenciaException {
        return atividadesDoPeriodo(mes.atDay(1), mes.atEndOfMonth());
    }

    public Map<LocalDate, List<Atividade>> atividadesDoPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        Map<LocalDate, List<Atividade>> porDia = new TreeMap<>();
        for (Atividade atividade : atividadeRepositorio.listarPorPeriodo(inicio, fim)) {
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
        return atividadeRepositorio.listarPorPeriodo(hoje, hoje.plusDays(dias)).stream()
                .sorted()
                .toList();
    }

    static final int DIAS_AVISO_COMPETICAO = 7;

    static final int DIAS_PRESENCA_PENDENTE = 30;

    public List<Alerta> alertas(LocalDate hoje) throws PersistenciaException {
        List<Alerta> alertas = new ArrayList<>();
        alertas.addAll(alertaRepositorio.tarefasAtrasadas(hoje));
        alertas.addAll(alertaRepositorio.treinosSemPresenca(hoje, DIAS_PRESENCA_PENDENTE));
        alertas.addAll(alertaRepositorio.competicoesProximas(hoje, DIAS_AVISO_COMPETICAO));
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
            compromissoRepositorio.inserir(compromisso);
        } else {
            compromissoRepositorio.atualizar(compromisso);
        }
    }

    public void excluirCompromisso(Compromisso compromisso) throws PersistenciaException {
        compromissoRepositorio.excluir(compromisso.getId());
    }
}
