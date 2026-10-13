package br.com.athletiza.controller;

import br.com.athletiza.dao.CargoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelCadastroCargo;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller do cadastro de cargo e da hierarquia entre cargos (CH-18, RF07).
 */
public class CadastroCargoController {

    private final PainelCadastroCargo view;
    private final Cargo model;
    private final CargoRepository repository;
    private final Navegador navegador;

    public CadastroCargoController(PainelCadastroCargo view, Cargo model, CargoRepository repository,
            Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.navegador = navegador;
        preencherTela();
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoSalvar(e -> salvar());
        view.adicionarAcaoCancelar(e -> cancelar());
    }

    /** O cargo não aparece entre as opções de "Subordinado a". */
    private void preencherTela() {
        List<Cargo> outros = new ArrayList<>();
        try {
            for (Cargo cargo : repository.listarTodos()) {
                if (!cargo.equals(model)) {
                    outros.add(cargo);
                }
            }
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        view.preencherCampos(model, outros);
    }

    private void salvar() {
        try {
            gravar();
            view.mostrarMensagem("Cargo salvo com sucesso.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException {
        model.setNome(view.getNome());
        model.setOrdem(view.getNivel());
        model.setCargoSuperior(view.getSuperior());
        model.validar();
        model.verificarHierarquia();

        for (Cargo outro : repository.listarTodos()) {
            if (outro.equals(model) && !outro.getId().equals(model.getId())) {
                throw new RegraNegocioException("O cargo " + model.getNome() + " já está cadastrado.");
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

    public PainelCadastroCargo getView() {
        return view;
    }
}
