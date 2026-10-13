package br.com.athletiza.controller;

import br.com.athletiza.dao.EventoRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.eventos.PainelGerenciarEvento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller dos responsáveis e tarefas do evento (CH-41). As tarefas têm o
 * próprio controller (TarefasController); este cuida dos responsáveis e do
 * aviso de tarefas atrasadas.
 */
public class GerenciarEventoController {

    private final PainelGerenciarEvento view;
    private final Evento original;
    private final EventoRepository repository;
    private final MembroRepository membroRepository;
    private final Navegador navegador;
    /** Evento lido de novo do banco, com responsáveis e tarefas. */
    private Evento evento;

    public GerenciarEventoController(PainelGerenciarEvento view, Evento original, EventoRepository repository,
            TarefaRepository tarefaRepository, MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.original = original;
        this.repository = repository;
        this.membroRepository = membroRepository;
        this.navegador = navegador;
        this.evento = original;
        new TarefasController(view.getPainelTarefas(), tarefaRepository, membroRepository,
                TarefaRepository.Dono.EVENTO, original.getId(), () -> carregarEvento());
        view.exibirCabecalho(original);
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoVoltar(e -> navegador.voltar());
        view.adicionarAcaoAdicionarResponsavel(e -> adicionarResponsavel());
        view.adicionarAcaoRemoverResponsavel(e -> removerResponsavel());
    }

    private void carregar() {
        carregarEvento();
        view.getPainelTarefas().carregar();
    }

    /** Lê o evento novamente do banco e atualiza responsáveis e o aviso de atrasadas. */
    private void carregarEvento() {
        Evento atualizado = new Evento(original.getTitulo(), original.getTipoEvento(), original.getData(),
                original.getHorario(), original.getLocal());
        atualizado.setId(original.getId());
        try {
            repository.carregarDetalhes(atualizado);
            evento = atualizado;
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        view.exibirResponsaveis(new ArrayList<>(evento.getResponsaveis()));
        view.exibirTarefasAtrasadas(evento.getTarefasAtrasadas(LocalDate.now()).size());
    }

    private void adicionarResponsavel() {
        try {
            List<Membro> disponiveis = new ArrayList<>();
            for (Membro membro : membroRepository.listarAtivos()) {
                if (!evento.getResponsaveis().contains(membro)) {
                    disponiveis.add(membro);
                }
            }
            if (disponiveis.isEmpty()) {
                view.mostrarAviso("Todos os membros ativos já são responsáveis por este evento.");
                return;
            }
            if (view.exibirFormularioResponsavel(disponiveis)) {
                adicionarResponsavel(view.getMembroEscolhido());
                carregarEvento();
            }
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    /** Associa um membro à organização do evento (CH-41). Um membro não pode ser associado duas vezes. */
    void adicionarResponsavel(Membro membro) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(membro, "Membro").validar();
        if (!evento.adicionarResponsavel(membro)) {
            throw new RegraNegocioException(membro.getNome() + " já é responsável por este evento.");
        }
        try {
            repository.adicionarResponsavel(evento, membro);
        } catch (PersistenciaException e) {
            evento.removerResponsavel(membro); // desfaz na memória e relança para a tela mostrar
            throw e;
        }
    }

    private void removerResponsavel() {
        Membro membro = view.getResponsavelSelecionado();
        if (!view.confirmar("Remover " + membro.getNome() + " dos responsáveis pelo evento?")) {
            return;
        }
        try {
            repository.removerResponsavel(evento, membro);
            evento.removerResponsavel(membro);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregarEvento();
    }

    /** Evento como está no controller (usado nos testes). */
    Evento getEvento() {
        return evento;
    }

    public PainelGerenciarEvento getView() {
        return view;
    }
}
