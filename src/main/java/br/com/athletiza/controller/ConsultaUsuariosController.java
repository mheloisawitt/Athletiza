package br.com.athletiza.controller;

import br.com.athletiza.dao.UsuarioRepository;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.usuarios.PainelCadastroUsuario;
import br.com.athletiza.view.usuarios.PainelConsultaUsuarios;

/**
 * Controller da consulta de usuários (RF01). Disponível apenas para administradores (CH-13).
 */
public class ConsultaUsuariosController {

    private final PainelConsultaUsuarios view;
    private final UsuarioRepository repository;
    private final Navegador navegador;

    public ConsultaUsuariosController(PainelConsultaUsuarios view, UsuarioRepository repository, Navegador navegador) {
        this.view = view;
        this.repository = repository;
        this.navegador = navegador;
        adicionarAcoesBotoes();
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoCarregar(e -> carregar());
        view.adicionarAcaoIncluir(e -> abrirCadastro(new Usuario()));
        view.adicionarAcaoEditar(e -> abrirCadastro(view.getSelecionado()));
        view.adicionarAcaoExcluir(e -> excluirSelecionado());
    }

    private void carregar() {
        try {
            view.exibir(repository.listarTodos());
        } catch (PersistenciaException e) {
            view.mostrarErro(e);
        }
    }

    private void abrirCadastro(Usuario usuario) {
        PainelCadastroUsuario tela = new PainelCadastroUsuario(usuario.isNova());
        new CadastroUsuarioController(tela, usuario, repository, navegador);
        navegador.abrir(tela);
    }

    private void excluirSelecionado() {
        Usuario usuario = view.getSelecionado();
        if (!view.confirmar("Deseja realmente excluir \"" + usuario + "\"?")) {
            return;
        }
        try {
            excluir(usuario);
            view.mostrarMensagem("Usuário excluído.");
        } catch (AthletizaException e) {
            view.mostrarErro(e);
        }
        carregar();
    }

    /** Não é possível excluir a si mesmo nem o último administrador. */
    void excluir(Usuario usuario) throws RegraNegocioException, PersistenciaException {
        if (usuario.equals(Sessao.getUsuarioLogado())) {
            throw new RegraNegocioException("Você não pode excluir o seu próprio usuário.");
        }
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR
                && repository.contarOutrosAdministradoresAtivos(usuario.getId()) == 0) {
            throw new RegraNegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
        repository.excluir(usuario.getId());
    }

    public PainelConsultaUsuarios getView() {
        return view;
    }
}
