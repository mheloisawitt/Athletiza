package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaRepository;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.SituacaoAtleta;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.atletas.PainelCadastroAtleta;
import br.com.athletiza.view.componentes.Navegador;
import java.util.ArrayList;
import java.util.Map;

/**
 * Controller do cadastro de atleta (CH-26).
 */
public class CadastroAtletaController {

    private final PainelCadastroAtleta view;
    private final Atleta model;
    private final AtletaRepository repository;
    private final ModalidadeRepository modalidadeRepository;
    private final Navegador navegador;

    public CadastroAtletaController(PainelCadastroAtleta view, Atleta model, AtletaRepository repository,
            ModalidadeRepository modalidadeRepository, Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.modalidadeRepository = modalidadeRepository;
        this.navegador = navegador;
        preencherTela();
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoSalvar(e -> salvar());
        view.adicionarAcaoCancelar(e -> cancelar());
    }

    private void preencherTela() {
        try {
            view.preencherCampos(model, modalidadeRepository.listarTodos());
        } catch (PersistenciaException e) {
            view.preencherCampos(model, new ArrayList<Modalidade>());
            view.mostrarErro(e);
        }
    }

    private void salvar() {
        try {
            gravar();
            view.mostrarMensagem("Atleta salvo com sucesso.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    /** Lê a tela, valida e grava (atleta e modalidades na mesma transação). */
    void gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException {
        model.setNome(view.getNome());
        model.setMatricula(view.getMatricula());
        model.setDataNascimento(Validador.converterData(view.getDataNascimento(), "Data de nascimento"));
        model.setSituacao(view.getSituacao());
        model.setContato(view.getContato());
        model.setObservacoes(view.getObservacoes());
        model.desvincularTodas();
        for (Map.Entry<Modalidade, SituacaoAtleta> marcada : view.getModalidadesMarcadas().entrySet()) {
            model.vincularModalidade(marcada.getKey(), marcada.getValue());
        }
        model.validar();

        if (repository.existeMatricula(model.getMatricula(), model.getId())) {
            throw new RegraNegocioException("Já existe um atleta com a matrícula " + model.getMatricula() + ".");
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

    public PainelCadastroAtleta getView() {
        return view;
    }
}
