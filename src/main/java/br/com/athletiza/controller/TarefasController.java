package br.com.athletiza.controller;

import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.SituacaoTarefa;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.PainelTarefas;

/**
 * Controller das tarefas (RF19) de um evento ou de uma competição.
 * O mesmo controller serve para os dois: o "dono" diz de quem são as tarefas.
 */
public class TarefasController {

    private final PainelTarefas view;
    private final TarefaRepository repository;
    private final MembroRepository membroRepository;
    private final TarefaRepository.Dono dono;
    private final int idDono;
    /** Avisado depois de cada alteração (ex.: para atualizar o alerta de tarefas atrasadas). */
    private final Runnable aoAlterar;

    public TarefasController(PainelTarefas view, TarefaRepository repository, MembroRepository membroRepository,
            TarefaRepository.Dono dono, int idDono, Runnable aoAlterar) {
        this.view = view;
        this.repository = repository;
        this.membroRepository = membroRepository;
        this.dono = dono;
        this.idDono = idDono;
        this.aoAlterar = aoAlterar;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> editar(new Tarefa()));
        view.adicionarAcaoEditar(e -> editar(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionada());
        view.adicionarAcaoConcluir(e -> concluirSelecionada());
    }

    /** Tarefas por prazo (as sem prazo por último). */
    private void carregar() {
        try {
            view.exibir(repository.listar(dono, idDono));
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /** Formulário da tarefa; reabre enquanto houver erro, para o usuário corrigir. */
    private void editar(Tarefa tarefa) {
        try {
            view.preencherFormulario(tarefa, membroRepository.listarAtivos());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
            return;
        }
        while (view.exibirFormulario(tarefa.isNova() ? "Nova tarefa" : "Editar tarefa")) {
            try {
                tarefa.setDescricao(view.getDescricao());
                tarefa.setResponsavel(view.getResponsavel());
                tarefa.setPrazo(Validador.converterData(view.getPrazo(), "Prazo"));
                tarefa.setSituacao(view.getSituacao());
                salvar(tarefa);
                break;
            } catch (AthletizaException e) {
                view.mostrarErro(e);
            }
        }
        carregar();
        avisarAlteracao();
    }

    /** Inclui ou altera uma tarefa (CH-41). */
    void salvar(Tarefa tarefa) throws ValidacaoException, PersistenciaException {
        tarefa.validar();
        repository.salvar(dono, idDono, tarefa);
    }

    private void concluirSelecionada() {
        Tarefa tarefa = view.getSelecionado();
        tarefa.setSituacao(SituacaoTarefa.CONCLUIDA);
        try {
            salvar(tarefa);
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
        avisarAlteracao();
    }

    private void excluirSelecionada() {
        Tarefa tarefa = view.getSelecionado();
        if (!view.confirmar("Excluir a tarefa \"" + tarefa.getDescricao() + "\"?")) {
            return;
        }
        try {
            repository.excluir(tarefa);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregar();
        avisarAlteracao();
    }

    private void avisarAlteracao() {
        if (aoAlterar != null) {
            aoAlterar.run();
        }
    }

    public PainelTarefas getView() {
        return view;
    }
}
