package br.com.athletiza.controller;

import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelCadastroMembro;

/**
 * Controller do cadastro de membro (CH-22).
 */
public class CadastroMembroController {

    private final PainelCadastroMembro view;
    private final Membro model;
    private final MembroRepository repository;
    private final Navegador navegador;

    public CadastroMembroController(PainelCadastroMembro view, Membro model, MembroRepository repository,
            Navegador navegador) {
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
        try {
            gravar();
            view.mostrarMensagem("Membro salvo com sucesso.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException {
        model.setNome(view.getNome());
        model.setMatricula(view.getMatricula());
        model.setContato(view.getContato());
        model.setSituacao(view.getSituacao());
        model.validar();

        if (repository.existeMatricula(model.getMatricula(), model.getId())) {
            throw new RegraNegocioException("Já existe um membro com a matrícula " + model.getMatricula() + ".");
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

    public PainelCadastroMembro getView() {
        return view;
    }
}
