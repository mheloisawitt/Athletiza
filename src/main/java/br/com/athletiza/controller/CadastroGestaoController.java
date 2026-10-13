package br.com.athletiza.controller;

import br.com.athletiza.dao.GestaoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelCadastroGestao;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller do cadastro de gestão (CH-20). Períodos sobrepostos são recusados.
 */
public class CadastroGestaoController {

    private final PainelCadastroGestao view;
    private final Gestao model;
    private final GestaoRepository repository;
    private final Navegador navegador;

    public CadastroGestaoController(PainelCadastroGestao view, Gestao model, GestaoRepository repository,
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
            view.mostrarMensagem("Gestão salva com sucesso.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void gravar() throws ValidacaoException, RegraNegocioException, PersistenciaException {
        model.setNome(view.getNome());
        model.setDataInicio(Validador.converterData(view.getDataInicio(), "Data de início"));
        model.setDataFim(Validador.converterData(view.getDataFim(), "Data de fim"));
        model.setSituacao(view.getSituacao());
        model.setDescricao(view.getDescricao());
        model.validar();

        List<Gestao> outras = new ArrayList<>();
        for (Gestao gestao : repository.listarTodos()) {
            if (!gestao.getId().equals(model.getId())) { // na edição, ignora a própria gestão
                outras.add(gestao);
            }
        }
        for (Gestao outra : outras) {
            if (model.sobrepoe(outra)) {
                throw new RegraNegocioException("O período informado se sobrepõe à " + outra.getNome() + " ("
                        + Validador.FORMATO_DATA.format(outra.getDataInicio()) + " a "
                        + Validador.FORMATO_DATA.format(outra.getDataFim()) + ").");
            }
        }
        for (Gestao outra : outras) {
            if (outra.getNome().equalsIgnoreCase(model.getNome().trim())) {
                throw new RegraNegocioException("Já existe uma gestão chamada " + model.getNome() + ".");
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

    public PainelCadastroGestao getView() {
        return view;
    }
}
