package br.com.athletiza.controller;

import br.com.athletiza.dao.AlertaRepository;
import br.com.athletiza.dao.AtividadeRepository;
import br.com.athletiza.dao.CompeticaoRepository;
import br.com.athletiza.dao.CompromissoRepository;
import br.com.athletiza.dao.EventoRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.dao.TreinoRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Alerta;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Treino;
import br.com.athletiza.view.calendario.DialogoCompromisso;
import br.com.athletiza.view.calendario.PainelCalendario;
import br.com.athletiza.view.competicoes.PainelCadastroCompeticao;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.eventos.PainelCadastroEvento;
import br.com.athletiza.view.treinos.PainelCadastroTreino;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Controller do calendário da tela inicial (CH-15 a CH-17, RF09, RF10, RF24).
 */
public class CalendarioController {

    /** Dias de antecedência para avisar sobre competições. */
    static final int DIAS_AVISO_COMPETICAO = 7;
    /** Quantos dias para trás procurar treinos sem lista de presença. */
    static final int DIAS_PRESENCA_PENDENTE = 30;

    private final PainelCalendario view;
    private final AtividadeRepository atividadeRepository;
    private final AlertaRepository alertaRepository;
    private final CompromissoRepository compromissoRepository;
    private final TreinoRepository treinoRepository;
    private final CompeticaoRepository competicaoRepository;
    private final EventoRepository eventoRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final MembroRepository membroRepository;
    private final Navegador navegador;

    public CalendarioController(PainelCalendario view, AtividadeRepository atividadeRepository,
            AlertaRepository alertaRepository, CompromissoRepository compromissoRepository,
            TreinoRepository treinoRepository, CompeticaoRepository competicaoRepository,
            EventoRepository eventoRepository, ModalidadeRepository modalidadeRepository,
            MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.atividadeRepository = atividadeRepository;
        this.alertaRepository = alertaRepository;
        this.compromissoRepository = compromissoRepository;
        this.treinoRepository = treinoRepository;
        this.competicaoRepository = competicaoRepository;
        this.eventoRepository = eventoRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.membroRepository = membroRepository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoNovoCompromisso(e -> novoCompromisso(view.getDiaDoNovoCompromisso()));
        view.adicionarAcaoEditarCompromisso(e -> editarCompromisso((Compromisso) view.getAtividadeClicada()));
        view.adicionarAcaoExcluirCompromisso(e -> excluirCompromisso((Compromisso) view.getAtividadeClicada()));
        view.adicionarAcaoAbrirAtividade(e -> abrir(view.getAtividadeClicada()));
    }

    /** Busca as atividades do período exibido, os próximos eventos e os alertas. */
    private void carregar() {
        LocalDate hoje = LocalDate.now();
        try {
            view.exibir(atividadesDoPeriodo(view.getInicioDoPeriodo(), view.getFimDoPeriodo()),
                    proximasAtividades(hoje, view.getDiasProximos()), alertas(hoje));
        } catch (PersistenciaException e) {
            view.exibir(new HashMap<LocalDate, List<Atividade>>(), new ArrayList<Atividade>(), new ArrayList<Alerta>());
            view.mostrarErro(e);
        }
    }

    /**
     * Atividades do mês agrupadas por dia (CH-15). Uma competição de vários dias
     * aparece em todos eles. As listas de cada dia vêm ordenadas por horário.
     */
    Map<LocalDate, List<Atividade>> atividadesDoMes(YearMonth mes) throws PersistenciaException {
        return atividadesDoPeriodo(mes.atDay(1), mes.atEndOfMonth());
    }

    /** Atividades de um período qualquer (ex.: uma semana, RF10), agrupadas por dia (TreeMap: em ordem de data). */
    Map<LocalDate, List<Atividade>> atividadesDoPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException {
        Map<LocalDate, List<Atividade>> porDia = new TreeMap<>();
        for (Atividade atividade : atividadeRepository.listarPorPeriodo(inicio, fim)) {
            for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
                if (atividade.ocorreEm(dia)) {
                    if (!porDia.containsKey(dia)) {
                        porDia.put(dia, new ArrayList<Atividade>());
                    }
                    porDia.get(dia).add(atividade);
                }
            }
        }
        for (List<Atividade> doDia : porDia.values()) {
            Collections.sort(doDia);
        }
        return porDia;
    }

    /**
     * Próximas atividades a partir de hoje, ordenadas por data e horário (CH-17).
     * Competições em andamento também aparecem.
     *
     * @param dias quantos dias à frente considerar (ex.: 7, 15, 30)
     */
    List<Atividade> proximasAtividades(LocalDate hoje, int dias) throws PersistenciaException {
        List<Atividade> proximas = atividadeRepository.listarPorPeriodo(hoje, hoje.plusDays(dias));
        Collections.sort(proximas);
        return proximas;
    }

    /**
     * Alertas da tela inicial (RF24): tarefas atrasadas, treinos sem presença
     * registrada e competições da próxima semana, do mais urgente ao menos urgente.
     */
    List<Alerta> alertas(LocalDate hoje) throws PersistenciaException {
        List<Alerta> alertas = new ArrayList<>();
        alertas.addAll(alertaRepository.tarefasAtrasadas(hoje));
        alertas.addAll(alertaRepository.treinosSemPresenca(hoje, DIAS_PRESENCA_PENDENTE));
        alertas.addAll(alertaRepository.competicoesProximas(hoje, DIAS_AVISO_COMPETICAO));
        Collections.sort(alertas);
        return alertas;
    }

    private void novoCompromisso(LocalDate dia) {
        Compromisso compromisso = new Compromisso();
        compromisso.setData(dia);
        if (abrirDialogoCompromisso(compromisso)) {
            view.irParaDia(compromisso.getData());
            carregar();
        }
    }

    private void editarCompromisso(Compromisso compromisso) {
        if (abrirDialogoCompromisso(compromisso)) {
            view.irParaDia(compromisso.getData());
        }
        carregar();
    }

    /** Abre o diálogo (modal: espera ser fechado) e indica se o compromisso foi salvo. */
    private boolean abrirDialogoCompromisso(Compromisso compromisso) {
        DialogoCompromisso dialogo = new DialogoCompromisso(view,
                compromisso.isNova() ? "Novo compromisso" : "Editar compromisso");
        CompromissoController controller = new CompromissoController(dialogo.getFormulario(), compromisso,
                compromissoRepository, dialogo);
        dialogo.setVisible(true);
        return controller.isSalvou();
    }

    private void excluirCompromisso(Compromisso compromisso) {
        if (!view.confirmar("Deseja realmente excluir o compromisso \"" + compromisso.getTitulo() + "\"?")) {
            return;
        }
        try {
            compromissoRepository.excluir(compromisso.getId());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /**
     * Abre treino, competição ou evento na tela de edição do seu módulo (RF09).
     * Ao salvar ou cancelar, o "Voltar" retorna ao calendário já atualizado.
     */
    private void abrir(Atividade atividade) {
        try {
            Atividade completa = carregarCompleta(atividade);
            if (completa instanceof Treino) {
                Treino treino = (Treino) completa;
                PainelCadastroTreino tela = new PainelCadastroTreino(treino instanceof Amistoso);
                new CadastroTreinoController(tela, treino, treinoRepository, modalidadeRepository, membroRepository,
                        navegador);
                navegador.abrir(tela);
            } else if (completa instanceof Competicao) {
                PainelCadastroCompeticao tela = new PainelCadastroCompeticao();
                new CadastroCompeticaoController(tela, (Competicao) completa, competicaoRepository, navegador);
                navegador.abrir(tela);
            } else if (completa instanceof Evento) {
                PainelCadastroEvento tela = new PainelCadastroEvento();
                new CadastroEventoController(tela, (Evento) completa, eventoRepository, navegador);
                navegador.abrir(tela);
            }
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /**
     * Lê a atividade completa do banco (o calendário carrega só o resumo), para
     * abrir a tela de edição a partir do calendário (RF09).
     */
    Atividade carregarCompleta(Atividade atividade) throws PersistenciaException {
        Atividade completa;
        if (atividade instanceof Treino) {
            completa = treinoRepository.buscarPorId(atividade.getId());
        } else if (atividade instanceof Competicao) {
            completa = competicaoRepository.buscarPorId(atividade.getId());
        } else if (atividade instanceof Evento) {
            completa = eventoRepository.buscarPorId(atividade.getId());
        } else {
            completa = atividade;
        }
        if (completa == null) {
            throw new PersistenciaException("Esta atividade não existe mais.");
        }
        return completa;
    }

    public PainelCalendario getView() {
        return view;
    }
}
