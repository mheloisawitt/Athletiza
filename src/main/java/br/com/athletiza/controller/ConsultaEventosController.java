package br.com.athletiza.controller;

import br.com.athletiza.dao.EventoRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.eventos.PainelCadastroEvento;
import br.com.athletiza.view.eventos.PainelConsultaEventos;
import br.com.athletiza.view.eventos.PainelGerenciarEvento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller da consulta de eventos (CH-39).
 */
public class ConsultaEventosController {

    private final PainelConsultaEventos view;
    private final EventoRepository repository;
    private final TarefaRepository tarefaRepository;
    private final MembroRepository membroRepository;
    private final Navegador navegador;

    public ConsultaEventosController(PainelConsultaEventos view, EventoRepository repository,
            TarefaRepository tarefaRepository, MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.tarefaRepository = tarefaRepository;
        this.membroRepository = membroRepository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoFiltrar(e -> carregar());
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Evento()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoGerenciar(e -> abrirGerenciar(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionado());
    }

    private void carregar() {
        LocalDate hoje = LocalDate.now();
        PainelConsultaEventos.Periodo periodo = view.getPeriodo();
        LocalDate inicio = periodo == PainelConsultaEventos.Periodo.PROXIMOS ? hoje : null;
        LocalDate fim = periodo == PainelConsultaEventos.Periodo.REALIZADOS ? hoje.minusDays(1) : null;
        try {
            view.exibir(pesquisar(view.getTipo(), inicio, fim));
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /**
     * Eventos em ordem de data (ordem natural de Atividade), com filtros opcionais.
     *
     * @param tipo   apenas eventos desse tipo (null = todos)
     * @param inicio apenas eventos a partir desta data (null = sem limite)
     * @param fim    apenas eventos até esta data (null = sem limite)
     */
    List<Evento> pesquisar(TipoEvento tipo, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Evento> encontrados = new ArrayList<>();
        for (Evento evento : repository.listarTodos()) {
            boolean tipoConfere = tipo == null || evento.getTipoEvento() == tipo;
            boolean inicioConfere = inicio == null || !evento.getData().isBefore(inicio);
            boolean fimConfere = fim == null || !evento.getData().isAfter(fim);
            if (tipoConfere && inicioConfere && fimConfere) {
                encontrados.add(evento);
            }
        }
        Collections.sort(encontrados);
        return encontrados;
    }

    private void abrirCadastro(Evento evento) {
        PainelCadastroEvento tela = new PainelCadastroEvento();
        new CadastroEventoController(tela, evento, repository, navegador);
        navegador.abrir(tela);
    }

    private void abrirGerenciar(Evento evento) {
        PainelGerenciarEvento tela = new PainelGerenciarEvento();
        new GerenciarEventoController(tela, evento, repository, tarefaRepository, membroRepository, navegador);
        navegador.abrir(tela);
    }

    /** Exclui o evento junto com responsáveis e tarefas (em cascata no banco). */
    private void excluirSelecionado() {
        Evento evento = view.getSelecionado();
        String pergunta;
        if (evento.getTarefas().isEmpty()) {
            pergunta = "Deseja realmente excluir \"" + evento + "\"?";
        } else {
            pergunta = evento.getTitulo() + " possui " + evento.getTarefas().size()
                    + " tarefa(s), que também serão excluídas, assim como os responsáveis. Continuar?";
        }
        if (!view.confirmar(pergunta)) {
            return;
        }
        try {
            repository.excluir(evento.getId());
            view.mostrarMensagem("Evento excluído.");
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    public PainelConsultaEventos getView() {
        return view;
    }
}
