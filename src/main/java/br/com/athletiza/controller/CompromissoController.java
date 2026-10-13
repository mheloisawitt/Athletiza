package br.com.athletiza.controller;

import br.com.athletiza.dao.CompromissoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Compromisso;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.calendario.PainelCompromisso;
import br.com.athletiza.view.componentes.Navegador;

/**
 * Controller do formulário de compromisso do calendário (CH-16).
 */
public class CompromissoController {

    private final PainelCompromisso view;
    private final Compromisso model;
    private final CompromissoRepository repository;
    private final Navegador navegador;
    private boolean salvou;

    public CompromissoController(PainelCompromisso view, Compromisso model, CompromissoRepository repository,
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
            salvou = true;
            view.mostrarMensagem("Compromisso salvo com sucesso.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void gravar() throws ValidacaoException, PersistenciaException {
        model.setTitulo(view.getTituloCompromisso());
        model.setData(Validador.converterData(view.getData(), "Data"));
        model.setHorario(Validador.converterHorario(view.getHorario(), "Horário"));
        model.setLocal(view.getLocal());
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

    /** Indica se o compromisso foi salvo (o calendário então mostra o dia dele). */
    public boolean isSalvou() {
        return salvou;
    }
}
