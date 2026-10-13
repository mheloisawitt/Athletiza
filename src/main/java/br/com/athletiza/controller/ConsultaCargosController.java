package br.com.athletiza.controller;

import br.com.athletiza.dao.CargoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelCadastroCargo;
import br.com.athletiza.view.gestao.PainelConsultaCargos;
import java.util.Collections;
import java.util.List;

/**
 * Controller da consulta de cargos (CH-18).
 */
public class ConsultaCargosController {

    private final PainelConsultaCargos view;
    private final CargoRepository repository;
    private final Navegador navegador;

    public ConsultaCargosController(PainelConsultaCargos view, CargoRepository repository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Cargo()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionado());
        view.adicionarAcaoVoltar(e -> navegador.voltar());
    }

    /** Cargos em ordem hierárquica (nível e nome). */
    private void carregar() {
        try {
            List<Cargo> cargos = repository.listarTodos();
            Collections.sort(cargos);
            view.exibir(cargos);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    private void abrirCadastro(Cargo cargo) {
        PainelCadastroCargo tela = new PainelCadastroCargo();
        new CadastroCargoController(tela, cargo, repository, navegador);
        navegador.abrir(tela);
    }

    private void excluirSelecionado() {
        Cargo cargo = view.getSelecionado();
        if (!view.confirmar("Deseja realmente excluir \"" + cargo + "\"?")) {
            return;
        }
        try {
            excluir(cargo);
            view.mostrarMensagem("Cargo excluído.");
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Só exclui cargos que não estão ocupados em nenhuma gestão. */
    void excluir(Cargo cargo) throws RegraNegocioException, PersistenciaException {
        int ocupacoes = repository.contarOcupacoes(cargo.getId());
        if (ocupacoes > 0) {
            throw new RegraNegocioException("Não é possível excluir o cargo " + cargo.getNome()
                    + ": ele está ocupado " + ocupacoes + " vez(es) em gestões.");
        }
        repository.excluir(cargo.getId());
    }

    public PainelConsultaCargos getView() {
        return view;
    }
}
