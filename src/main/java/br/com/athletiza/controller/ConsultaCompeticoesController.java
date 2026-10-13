package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaRepository;
import br.com.athletiza.dao.CompeticaoRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.dao.TarefaRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.view.competicoes.PainelCadastroCompeticao;
import br.com.athletiza.view.competicoes.PainelConsultaCompeticoes;
import br.com.athletiza.view.competicoes.PainelGerenciarCompeticao;
import br.com.athletiza.view.componentes.Navegador;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller da consulta de competições (CH-29): filtros, exclusão e abertura
 * do cadastro e do gerenciamento (inscrições, resultados e tarefas).
 */
public class ConsultaCompeticoesController {

    private final PainelConsultaCompeticoes view;
    private final CompeticaoRepository repository;
    private final AtletaRepository atletaRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final TarefaRepository tarefaRepository;
    private final MembroRepository membroRepository;
    private final Navegador navegador;

    public ConsultaCompeticoesController(PainelConsultaCompeticoes view, CompeticaoRepository repository,
            AtletaRepository atletaRepository, ModalidadeRepository modalidadeRepository,
            TarefaRepository tarefaRepository, MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.atletaRepository = atletaRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.tarefaRepository = tarefaRepository;
        this.membroRepository = membroRepository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoFiltrar(e -> carregar());
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Competicao()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoGerenciar(e -> abrirGerenciar(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionada());
    }

    private void carregar() {
        LocalDate hoje = LocalDate.now();
        PainelConsultaCompeticoes.Periodo periodo = view.getPeriodo();
        LocalDate inicio = periodo == PainelConsultaCompeticoes.Periodo.PROXIMAS ? hoje : null;
        LocalDate fim = periodo == PainelConsultaCompeticoes.Periodo.ANTERIORES ? hoje.minusDays(1) : null;
        try {
            view.exibir(pesquisar(view.getSituacao(), inicio, fim));
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /**
     * Competições ordenadas por data (ordem natural de Atividade), com filtros opcionais.
     *
     * @param situacao apenas competições nessa situação (null = todas)
     * @param inicio   apenas competições que terminam a partir desta data (null = sem limite)
     * @param fim      apenas competições que começam até esta data (null = sem limite)
     */
    List<Competicao> pesquisar(SituacaoCompeticao situacao, LocalDate inicio, LocalDate fim)
            throws PersistenciaException {
        List<Competicao> encontradas = new ArrayList<>();
        for (Competicao competicao : repository.listarTodos()) {
            boolean situacaoConfere = situacao == null || competicao.getSituacao() == situacao;
            boolean inicioConfere = inicio == null || !competicao.getTermino().isBefore(inicio);
            boolean fimConfere = fim == null || !competicao.getData().isAfter(fim);
            if (situacaoConfere && inicioConfere && fimConfere) {
                encontradas.add(competicao);
            }
        }
        Collections.sort(encontradas);
        return encontradas;
    }

    private void abrirCadastro(Competicao competicao) {
        PainelCadastroCompeticao tela = new PainelCadastroCompeticao();
        new CadastroCompeticaoController(tela, competicao, repository, navegador);
        navegador.abrir(tela);
    }

    private void abrirGerenciar(Competicao competicao) {
        PainelGerenciarCompeticao tela = new PainelGerenciarCompeticao();
        new GerenciarCompeticaoController(tela, competicao, repository, atletaRepository, modalidadeRepository,
                tarefaRepository, membroRepository, navegador);
        navegador.abrir(tela);
    }

    /** Avisa que inscrições e resultados também serão apagados (CH-29). */
    private void excluirSelecionada() {
        Competicao competicao = view.getSelecionado();
        try {
            repository.carregarInscricoes(competicao);
            int inscricoes = competicao.getTotalInscricoes();
            int resultados = competicao.getResultados().size();
            String pergunta;
            if (inscricoes > 0 || resultados > 0) {
                pergunta = competicao.getTitulo() + " possui " + inscricoes + " inscrição(ões) de atletas e "
                        + resultados + " resultado(s).\nEles também serão excluídos. Continuar?";
            } else {
                pergunta = "Deseja realmente excluir \"" + competicao + "\"?";
            }
            if (!view.confirmar(pergunta)) {
                return;
            }
            // Inscrições, resultados e tarefas são excluídos em cascata pelo banco
            repository.excluir(competicao.getId());
            view.mostrarMensagem("Competição excluída.");
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    public PainelConsultaCompeticoes getView() {
        return view;
    }
}
