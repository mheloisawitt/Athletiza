package br.com.athletiza.controller;

import br.com.athletiza.dao.EventoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Validador;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.eventos.PainelCadastroEvento;

/**
 * Controller do cadastro de evento (CH-40).
 */
public class CadastroEventoController {

    private final PainelCadastroEvento view;
    private final Evento model;
    private final EventoRepository repository;
    private final Navegador navegador;

    public CadastroEventoController(PainelCadastroEvento view, Evento model, EventoRepository repository,
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
            view.mostrarMensagem("Evento salvo com sucesso. Ele já aparece no calendário.");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    void gravar() throws ValidacaoException, PersistenciaException {
        model.setTitulo(view.getNome());
        model.setTipoEvento(view.getTipo());
        model.setData(Validador.converterData(view.getData(), "Data"));
        model.setHorario(Validador.converterHorario(view.getHorario(), "Horário"));
        model.setLocal(view.getLocal());
        model.setSituacao(view.getSituacao());
        model.setDescricao(view.getDescricao());
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

    public PainelCadastroEvento getView() {
        return view;
    }
}
