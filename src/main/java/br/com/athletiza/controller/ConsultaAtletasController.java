package br.com.athletiza.controller;

import br.com.athletiza.dao.AtletaRepository;
import br.com.athletiza.dao.ModalidadeRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.view.atletas.PainelCadastroAtleta;
import br.com.athletiza.view.atletas.PainelConsultaAtletas;
import br.com.athletiza.view.atletas.PainelConsultaModalidades;
import br.com.athletiza.view.componentes.Navegador;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Controller da consulta de atletas (CH-24, CH-25): filtros, ordenação,
 * exclusão e abertura do cadastro e das modalidades.
 */
public class ConsultaAtletasController {

    private final PainelConsultaAtletas view;
    private final AtletaRepository repository;
    private final ModalidadeRepository modalidadeRepository;
    private final Navegador navegador;

    public ConsultaAtletasController(PainelConsultaAtletas view, AtletaRepository repository,
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
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Atleta()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionado());
        view.adicionarAcaoModalidades(e -> abrirModalidades());
    }

    private void carregar() {
        try {
            // As modalidades podem ter mudado no cadastro de modalidades
            view.setModalidades(modalidadeRepository.listarTodos());
            view.exibir(pesquisar(null, view.getModalidade(), view.getSituacao(), view.getOrdem()));
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    /**
     * Filtra os atletas (CH-24). Filtros nulos ou vazios são ignorados.
     *
     * @param texto      trecho do nome ou da matrícula
     * @param modalidade apenas atletas vinculados a ela
     * @param situacao   apenas atletas nessa situação
     * @param ordem      critério de ordenação (ex.: Atleta.POR_SITUACAO); null = por nome
     */
    List<Atleta> pesquisar(String texto, Modalidade modalidade, Situacao situacao, Comparator<Atleta> ordem)
            throws PersistenciaException {
        String busca = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        List<Atleta> encontrados = new ArrayList<>();
        for (Atleta atleta : repository.listarTodos()) {
            boolean textoConfere = busca.isEmpty() || atleta.getNome().toLowerCase(Locale.ROOT).contains(busca)
                    || atleta.getMatricula().contains(busca);
            boolean modalidadeConfere = modalidade == null || atleta.praticaModalidade(modalidade);
            boolean situacaoConfere = situacao == null || atleta.getSituacao() == situacao;
            if (textoConfere && modalidadeConfere && situacaoConfere) {
                encontrados.add(atleta);
            }
        }
        if (ordem == null) {
            Collections.sort(encontrados);
        } else {
            Collections.sort(encontrados, ordem);
        }
        return encontrados;
    }

    private void abrirCadastro(Atleta atleta) {
        PainelCadastroAtleta tela = new PainelCadastroAtleta();
        new CadastroAtletaController(tela, atleta, repository, modalidadeRepository, navegador);
        navegador.abrir(tela);
    }

    private void abrirModalidades() {
        PainelConsultaModalidades tela = new PainelConsultaModalidades();
        new ConsultaModalidadesController(tela, modalidadeRepository, navegador);
        navegador.abrir(tela);
    }

    private void excluirSelecionado() {
        Atleta atleta = view.getSelecionado();
        if (!view.confirmar("Deseja realmente excluir \"" + atleta + "\"?")) {
            return;
        }
        try {
            excluir(atleta);
            view.mostrarMensagem("Atleta excluído.");
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Exclui o atleta, a menos que tenha presenças, inscrições ou resultados (CH-45). */
    void excluir(Atleta atleta) throws RegraNegocioException, PersistenciaException {
        List<String> vinculos = repository.listarVinculos(atleta.getId());
        if (!vinculos.isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir " + atleta.getNome() + ", pois possui:\n- "
                    + String.join("\n- ", vinculos)
                    + "\n\nPara retirá-lo(a) das listagens, altere a situação para Inativo.");
        }
        repository.excluir(atleta.getId());
    }

    public PainelConsultaAtletas getView() {
        return view;
    }
}
