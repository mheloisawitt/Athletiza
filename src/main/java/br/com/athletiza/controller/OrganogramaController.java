package br.com.athletiza.controller;

import br.com.athletiza.dao.GestaoRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelOrganograma;

/**
 * Controller do organograma da gestão (CH-23, RF08). Lê a composição do banco
 * sempre que a tela é aberta e manda a View desenhar.
 */
public class OrganogramaController {

    private final PainelOrganograma view;
    private final Gestao model;
    private final GestaoRepository repository;
    private final Navegador navegador;

    public OrganogramaController(PainelOrganograma view, Gestao model, GestaoRepository repository,
            Navegador navegador) {
        this.view = view;
        this.model = model;
        this.repository = repository;
        this.navegador = navegador;
        view.setNomeGestao(model.getNome());
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoVoltar(e -> navegador.voltar());
    }

    private void carregar() {
        Gestao atualizada = new Gestao(model.getNome(), model.getDataInicio(), model.getDataFim());
        atualizada.setId(model.getId());
        try {
            repository.carregarComposicao(atualizada);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        view.exibir(atualizada.getOrganograma());
    }

    public PainelOrganograma getView() {
        return view;
    }
}
