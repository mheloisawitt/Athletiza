package br.com.athletiza.controller;

import br.com.athletiza.dao.MembroRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.gestao.PainelCadastroMembro;
import br.com.athletiza.view.gestao.PainelConsultaMembros;
import java.util.List;

/**
 * Controller da consulta de membros (CH-22).
 */
public class ConsultaMembrosController {

    private final PainelConsultaMembros view;
    private final MembroRepository repository;
    private final Navegador navegador;

    public ConsultaMembrosController(PainelConsultaMembros view, MembroRepository repository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Membro()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionado());
        view.adicionarAcaoVoltar(e -> navegador.voltar());
    }

    /** Membros em ordem alfabética (o DAO já ordena pelo nome). */
    private void carregar() {
        try {
            view.exibir(repository.listarTodos());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    private void abrirCadastro(Membro membro) {
        PainelCadastroMembro tela = new PainelCadastroMembro();
        new CadastroMembroController(tela, membro, repository, navegador);
        navegador.abrir(tela);
    }

    private void excluirSelecionado() {
        Membro membro = view.getSelecionado();
        if (!view.confirmar("Deseja realmente excluir \"" + membro + "\"?")) {
            return;
        }
        try {
            excluir(membro);
            view.mostrarMensagem("Membro excluído.");
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Não exclui membros com cargos, eventos ou tarefas (CH-45). */
    void excluir(Membro membro) throws RegraNegocioException, PersistenciaException {
        List<String> vinculos = repository.listarVinculos(membro.getId());
        if (!vinculos.isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir " + membro.getNome() + ", pois possui:\n- "
                    + String.join("\n- ", vinculos)
                    + "\n\nPara retirá-lo(a) das listagens, altere a situação para Inativo.");
        }
        repository.excluir(membro.getId());
    }

    public PainelConsultaMembros getView() {
        return view;
    }
}
