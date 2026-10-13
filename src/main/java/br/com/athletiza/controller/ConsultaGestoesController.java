package br.com.athletiza.controller;

import br.com.athletiza.dao.CargoRepository;
import br.com.athletiza.dao.GestaoRepository;
import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelCadastroGestao;
import br.com.athletiza.view.gestao.PainelConsultaCargos;
import br.com.athletiza.view.gestao.PainelConsultaGestoes;
import br.com.athletiza.view.gestao.PainelConsultaMembros;
import br.com.athletiza.view.gestao.PainelGerenciarGestao;
import java.util.Collections;
import java.util.List;

/**
 * Controller da consulta de gestões (CH-19). Abre o cadastro, o gerenciamento
 * da gestão e as consultas de cargos e de membros.
 */
public class ConsultaGestoesController {

    private final PainelConsultaGestoes view;
    private final GestaoRepository repository;
    private final CargoRepository cargoRepository;
    private final MembroRepository membroRepository;
    private final Navegador navegador;

    public ConsultaGestoesController(PainelConsultaGestoes view, GestaoRepository repository,
            CargoRepository cargoRepository, MembroRepository membroRepository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.cargoRepository = cargoRepository;
        this.membroRepository = membroRepository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Gestao()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionada());
        view.adicionarAcaoGerenciar(e -> abrirGerenciar(view.getSelecionado()));
        view.adicionarAcaoCargos(e -> abrirCargos());
        view.adicionarAcaoMembros(e -> abrirMembros());
    }

    /** Gestões ordenadas por período (ordem natural de Gestao, CH-19). */
    private void carregar() {
        try {
            List<Gestao> gestoes = repository.listarTodos();
            Collections.sort(gestoes);
            view.exibir(gestoes);
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    private void abrirCadastro(Gestao gestao) {
        PainelCadastroGestao tela = new PainelCadastroGestao();
        new CadastroGestaoController(tela, gestao, repository, navegador);
        navegador.abrir(tela);
    }

    private void abrirGerenciar(Gestao gestao) {
        PainelGerenciarGestao tela = new PainelGerenciarGestao();
        new GerenciarGestaoController(tela, gestao, repository, cargoRepository, membroRepository, navegador);
        navegador.abrir(tela);
    }

    private void abrirCargos() {
        PainelConsultaCargos tela = new PainelConsultaCargos();
        new ConsultaCargosController(tela, cargoRepository, navegador);
        navegador.abrir(tela);
    }

    private void abrirMembros() {
        PainelConsultaMembros tela = new PainelConsultaMembros();
        new ConsultaMembrosController(tela, membroRepository, navegador);
        navegador.abrir(tela);
    }

    /** Se a gestão tem membros em cargos, avisa que eles também serão removidos (CH-18). */
    private void excluirSelecionada() {
        Gestao gestao = view.getSelecionado();
        try {
            int membros = contarMembros(gestao);
            String pergunta = membros > 0
                    ? "A " + gestao.getNome() + " possui " + membros
                            + " membro(s) em cargos.\nEssa composição também será excluída. Continuar?"
                    : "Deseja realmente excluir \"" + gestao + "\"?";
            if (!view.confirmar(pergunta)) {
                return;
            }
            repository.excluir(gestao.getId());
            view.mostrarMensagem("Gestão excluída.");
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Quantos membros a gestão possui (lidos do banco). */
    int contarMembros(Gestao gestao) throws PersistenciaException {
        Gestao copia = new Gestao();
        copia.setId(gestao.getId());
        repository.carregarComposicao(copia);
        return copia.getComposicao().size();
    }

    public PainelConsultaGestoes getView() {
        return view;
    }
}
