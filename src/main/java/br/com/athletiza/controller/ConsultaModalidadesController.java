package br.com.athletiza.controller;

import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.atletas.PainelCadastroModalidade;
import br.com.athletiza.view.atletas.PainelConsultaModalidades;
import br.com.athletiza.view.componentes.Navegador;

/**
 * Controller da consulta de modalidades (CH-27).
 */
public class ConsultaModalidadesController {

    private final PainelConsultaModalidades view;
    private final ModalidadeRepository repository;
    private final Navegador navegador;

    public ConsultaModalidadesController(PainelConsultaModalidades view, ModalidadeRepository repository,
            Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Modalidade()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionada());
        view.adicionarAcaoVoltar(e -> navegador.voltar());
    }

    private void carregar() {
        try {
            view.exibir(repository.listarTodos());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    private void abrirCadastro(Modalidade modalidade) {
        PainelCadastroModalidade tela = new PainelCadastroModalidade();
        new CadastroModalidadeController(tela, modalidade, repository, navegador);
        navegador.abrir(tela);
    }

    private void excluirSelecionada() {
        Modalidade modalidade = view.getSelecionado();
        if (!view.confirmar("Deseja realmente excluir \"" + modalidade + "\"?")) {
            return;
        }
        try {
            excluir(modalidade);
            view.mostrarMensagem("Modalidade excluída.");
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Só exclui modalidades sem atletas, treinos ou competições vinculados. */
    void excluir(Modalidade modalidade) throws RegraNegocioException, PersistenciaException {
        if (repository.possuiVinculos(modalidade.getId())) {
            throw new RegraNegocioException("Não é possível excluir a modalidade " + modalidade
                    + ": existem atletas, treinos ou competições vinculados a ela.");
        }
        repository.excluir(modalidade.getId());
    }

    public PainelConsultaModalidades getView() {
        return view;
    }
}
