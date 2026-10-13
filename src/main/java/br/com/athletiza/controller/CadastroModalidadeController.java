package br.com.athletiza.controller;

import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Genero;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.atletas.PainelCadastroModalidade;
import br.com.athletiza.view.componentes.Navegador;

/**
 * Controller do cadastro de modalidade (CH-27).
 *
 * Possui a View e o Model como atributos, recebidos no construtor, e o
 * repositório pela interface (Inversão de Dependência). Cada botão chama
 * um único fluxo de caso de uso.
 */
public class CadastroModalidadeController {

    private final PainelCadastroModalidade view;
    private final Modalidade model;
    private final ModalidadeRepository repository;
    private final Navegador navegador;

    public CadastroModalidadeController(PainelCadastroModalidade view, Modalidade model,
            ModalidadeRepository repository, Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.navegador = navegador;
        view.preencherCampos(model);
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoSalvar(e -> salvar());
        view.adicionarAcaoCancelar(e -> cancelar());
    }

    private void salvar() {
        // A modalidade pode estar na tabela da consulta: se der erro, volta aos valores anteriores
        String nomeAnterior = model.getNome();
        Genero generoAnterior = model.getGenero();
        try {
            gravar();
            view.mostrarMensagem("Modalidade salva com sucesso.");
            navegador.voltar();
        } catch (AthletizaException e) {
            model.setNome(nomeAnterior);
            model.setGenero(generoAnterior);
            view.mostrarErro(e);
        }
    }

    /** Lê a tela, valida e grava. As exceções sobem para salvar(), que mostra a mensagem. */
    void gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException {
        model.setNome(view.getNome());
        model.setGenero(view.getGenero());
        model.validar();

        for (Modalidade outra : repository.listarTodos()) {
            if (outra.equals(model) && !outra.getId().equals(model.getId())) {
                throw new RegraNegocioException("A modalidade " + model + " já está cadastrada.");
            }
        }
        if (model.isNova()) {
            repository.inserir(model);
        } else {
            repository.atualizar(model);
        }
    }

    private void cancelar() {
        if (view.confirmar("Deseja sair sem salvar? As alterações serão perdidas.")) {
            navegador.voltar();
        }
    }

    public PainelCadastroModalidade getView() {
        return view;
    }
}
