package br.com.athletiza.controller;

import br.com.athletiza.dao.CompeticaoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.competicoes.PainelCadastroCompeticao;
import br.com.athletiza.view.componentes.Navegador;

/**
 * Controller do cadastro de competição (CH-30).
 */
public class CadastroCompeticaoController {

    private final PainelCadastroCompeticao view;
    private final Competicao model;
    private final CompeticaoRepository repository;
    private final Navegador navegador;

    public CadastroCompeticaoController(PainelCadastroCompeticao view, Competicao model,
            CompeticaoRepository repository, Navegador navegador) {
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
            view.mostrarMensagem("Competição salva com sucesso. Ela já aparece no calendário.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void gravar() throws ValidacaoException, PersistenciaException {
        model.setTitulo(view.getNome());
        model.setLocal(view.getLocal());
        model.setData(Validador.converterData(view.getDataInicio(), "Data de início"));
        model.setDataFim(Validador.converterData(view.getDataFim(), "Data de fim"));
        model.setHorario(Validador.converterHorario(view.getHorario(), "Horário"));
        model.setSituacao(view.getSituacao());
        model.setObservacoes(view.getObservacoes());
        model.validar();
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

    public PainelCadastroCompeticao getView() {
        return view;
    }
}
