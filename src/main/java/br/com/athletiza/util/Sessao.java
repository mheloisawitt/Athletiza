package br.com.athletiza.util;

import br.com.athletiza.model.Acesso;
import br.com.athletiza.model.Usuario;

public final class Sessao {
    private static Usuario usuarioLogado;
    private static Acesso acesso;

    private Sessao() {
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void iniciar(Usuario usuario, Acesso acessoDoUsuario) {
        usuarioLogado = usuario;
        acesso = acessoDoUsuario;
    }

    public static void encerrar() {
        usuarioLogado = null;
        acesso = null;
    }

    public static Acesso getAcesso() {
        return acesso == null ? Acesso.administrador() : acesso;
    }

    public static boolean podeGerenciarUsuarios() {
        return usuarioLogado != null && getAcesso().podeGerenciarUsuarios();
    }
}
