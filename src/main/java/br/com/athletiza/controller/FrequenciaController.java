package br.com.athletiza.controller;

import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.dao.TreinoRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.treinos.PainelFrequencia;
import br.com.athletiza.view.treinos.PainelHistoricoAtleta;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controller da frequência dos atletas nos treinos (CH-37).
 */
public class FrequenciaController {

    private final PainelFrequencia view;
    private final TreinoRepository repository;
    private final ModalidadeRepository modalidadeRepository;
    private final Navegador navegador;

    public FrequenciaController(PainelFrequencia view, TreinoRepository repository,
            ModalidadeRepository modalidadeRepository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.modalidadeRepository = modalidadeRepository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoFiltrar(e -> carregar());
        view.adicionarAcaoVerHistorico(e -> abrirHistorico(view.getSelecionado()));
        view.adicionarAcaoVoltar(e -> navegador.voltar());
    }

    private void carregar() {
        try {
            view.setModalidades(modalidadeRepository.listarTodos());
            LocalDate inicio = view.getPeriodo().inicio(LocalDate.now());
            view.exibir(frequencia(view.getModalidade(), inicio, null));
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /** Frequência por atleta no período, maior percentual primeiro (ordem natural de Frequencia). */
    List<Frequencia> frequencia(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        List<Frequencia> frequencias = repository.frequencia(modalidade, inicio, fim);
        Collections.sort(frequencias);
        return frequencias;
    }

    /** Em quais treinos o atleta esteve presente ou ausente, com os mesmos filtros. */
    private void abrirHistorico(Frequencia frequencia) {
        PainelHistoricoAtleta tela = new PainelHistoricoAtleta();
        new HistoricoAtletaController(tela, frequencia.getAtleta(), view.getModalidade(),
                view.getPeriodo().inicio(LocalDate.now()), repository, navegador);
        navegador.abrir(tela);
    }

    public PainelFrequencia getView() {
        return view;
    }
}
