package br.com.athletiza.controller;

import br.com.athletiza.dao.TreinoRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.treinos.PainelHistoricoAtleta;
import java.time.LocalDate;

/**
 * Controller do histórico de um atleta nos treinos (RF13). Apenas consulta.
 */
public class HistoricoAtletaController {

    private final PainelHistoricoAtleta view;
    private final Atleta atleta;
    private final Modalidade modalidade;
    private final LocalDate inicio;
    private final TreinoRepository repository;
    private final Navegador navegador;

    public HistoricoAtletaController(PainelHistoricoAtleta view, Atleta atleta, Modalidade modalidade,
            LocalDate inicio, TreinoRepository repository, Navegador navegador) {
        this.view = view;
        this.atleta = atleta;
        this.modalidade = modalidade;
        this.inicio = inicio;
        this.repository = repository;
        this.navegador = navegador;
        view.setNomeAtleta(atleta.getNome());
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoVoltar(e -> navegador.voltar());
    }

    /** Do treino mais recente ao mais antigo (o DAO já ordena). */
    private void carregar() {
        try {
            view.exibir(repository.historicoDoAtleta(atleta, modalidade, inicio));
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    public PainelHistoricoAtleta getView() {
        return view;
    }
}
