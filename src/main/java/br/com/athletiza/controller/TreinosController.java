package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.dao.TreinoRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Amistoso;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.ResumoPresenca;
import br.com.athletiza.model.TipoAtividade;
import br.com.athletiza.model.Treino;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.treinos.PainelCadastroTreino;
import br.com.athletiza.view.treinos.PainelFrequencia;
import br.com.athletiza.view.treinos.PainelPresenca;
import br.com.athletiza.view.treinos.PainelTreinos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller da consulta de treinos e amistosos (CH-34). Abre o cadastro,
 * a lista de presença e a frequência.
 */
public class TreinosController {

    private final PainelTreinos view;
    private final TreinoRepository repository;
    private final ModalidadeRepository modalidadeRepository;
    private final MembroRepository membroRepository;
    private final AtletaRepository atletaRepository;
    private final Navegador navegador;

    public TreinosController(PainelTreinos view, TreinoRepository repository, ModalidadeRepository modalidadeRepository,
            MembroRepository membroRepository, AtletaRepository atletaRepository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.modalidadeRepository = modalidadeRepository;
        this.membroRepository = membroRepository;
        this.atletaRepository = atletaRepository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoFiltrar(e -> carregar());
        view.adicionarAcaoIncluir(e -> incluir(new Treino()));
        view.adicionarAcaoAmistoso(e -> incluir(new Amistoso()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoGerenciar(e -> abrirPresenca(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionado());
        view.adicionarAcaoFrequencia(e -> abrirFrequencia());
    }

    private void carregar() {
        try {
            view.setModalidades(modalidadeRepository.listarTodos());
            LocalDate hoje = LocalDate.now();
            LocalDate inicio = view.getPeriodo() == PainelTreinos.Periodo.PROXIMOS ? hoje : null;
            LocalDate fim = view.getPeriodo() == PainelTreinos.Periodo.REALIZADOS ? hoje.minusDays(1) : null;
            List<Treino> treinos = pesquisar(view.getModalidade(), inicio, fim, view.getTipo());
            view.exibir(treinos, repository.resumirPresencas());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /**
     * Treinos e amistosos em ordem de data (ordem natural de Atividade).
     *
     * @param tipo TipoAtividade.TREINO, TipoAtividade.AMISTOSO ou null para ambos
     */
    List<Treino> pesquisar(Modalidade modalidade, LocalDate inicio, LocalDate fim, TipoAtividade tipo)
            throws PersistenciaException {
        List<Treino> encontrados = new ArrayList<>();
        for (Treino treino : repository.listar(modalidade, inicio, fim)) {
            if (tipo == null || treino.getTipo() == tipo) {
                encontrados.add(treino);
            }
        }
        Collections.sort(encontrados);
        return encontrados;
    }

    /** Novo treino ou amistoso, já na modalidade selecionada na lista. */
    private void incluir(Treino novo) {
        novo.setModalidade(view.getModalidade());
        abrirCadastro(novo);
    }

    private void abrirCadastro(Treino treino) {
        PainelCadastroTreino tela = new PainelCadastroTreino(treino instanceof Amistoso);
        new CadastroTreinoController(tela, treino, repository, modalidadeRepository, membroRepository, navegador);
        navegador.abrir(tela);
    }

    private void abrirPresenca(Treino treino) {
        PainelPresenca tela = new PainelPresenca();
        new PresencaController(tela, treino, repository, atletaRepository, navegador);
        navegador.abrir(tela);
    }

    private void abrirFrequencia() {
        PainelFrequencia tela = new PainelFrequencia();
        new FrequenciaController(tela, repository, modalidadeRepository, navegador);
        navegador.abrir(tela);
    }

    /** Exclui o treino junto com sua lista de presença (em cascata no banco). */
    private void excluirSelecionado() {
        Treino treino = view.getSelecionado();
        ResumoPresenca resumo = view.getResumoPresenca(treino);
        String pergunta;
        if (resumo == null) {
            pergunta = "Deseja realmente excluir \"" + treino + "\"?";
        } else {
            pergunta = "Este treino tem " + resumo.getRegistrados()
                    + " registro(s) de presença, que também serão excluídos. Continuar?";
        }
        if (!view.confirmar(pergunta)) {
            return;
        }
        try {
            repository.excluir(treino.getId());
            view.mostrarMensagem("Treino excluído.");
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    public PainelTreinos getView() {
        return view;
    }
}
