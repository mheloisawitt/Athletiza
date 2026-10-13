package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaRepository;
import br.com.athletiza.dao.TreinoRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.Treino;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.treinos.PainelPresenca;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller da lista de presença do treino (CH-36).
 */
public class PresencaController {

    private final PainelPresenca view;
    private final Treino model;
    private final TreinoRepository repository;
    private final AtletaRepository atletaRepository;
    private final Navegador navegador;

    public PresencaController(PainelPresenca view, Treino model, TreinoRepository repository,
            AtletaRepository atletaRepository, Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.atletaRepository = atletaRepository;
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
            repository.carregarPresencas(model);
            view.exibirChamada(model, atletasDaChamada());
        } catch (PersistenciaException e) {
            view.exibirChamada(model, new ArrayList<Atleta>());
            view.mostrarErro(e);
        }
    }

    /**
     * Atletas que aparecem na lista de presença: os ativos da modalidade
     * (em ordem alfabética) e os que já tinham presença registrada no treino.
     */
    List<Atleta> atletasDaChamada() throws PersistenciaException {
        List<Atleta> daModalidade = new ArrayList<>();
        for (Atleta atleta : atletaRepository.listarTodos()) {
            if (atleta.getSituacao() == Situacao.ATIVO && atleta.praticaModalidade(model.getModalidade())) {
                daModalidade.add(atleta);
            }
        }
        Collections.sort(daModalidade);
        Set<Atleta> chamada = new LinkedHashSet<>(daModalidade);
        chamada.addAll(model.getPresencas().keySet());
        return new ArrayList<>(chamada);
    }

    private void salvar() {
        try {
            gravar();
            Map<Atleta, Boolean> presencas = view.getPresencas();
            view.mostrarMensagem("Presença salva: " + model.getPresentes().size() + " de " + presencas.size()
                    + " atleta(s) presente(s).");
            navegador.voltar();
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
    }

    /** Grava a lista de presença de uma vez. Só treinos que já aconteceram. */
    void gravar() throws RegraNegocioException, PersistenciaException {
        for (Map.Entry<Atleta, Boolean> presenca : view.getPresencas().entrySet()) {
            model.registrarPresenca(presenca.getKey(), presenca.getValue());
        }
        if (model.getData() != null && model.getData().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Só é possível registrar presença em treinos que já aconteceram.");
        }
        repository.salvarPresencas(model);
    }

    private void cancelar() {
        if (view.confirmar("Deseja sair sem salvar? As alterações serão perdidas.")) {
            navegador.voltar();
        }
    }

    public PainelPresenca getView() {
        return view;
    }
}
