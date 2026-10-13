package br.com.athletiza.util;

import br.com.athletiza.model.Acesso;
import br.com.athletiza.model.Usuario;

/**
 * Guarda o usuário logado e o que ele pode fazer durante o uso do sistema.
 */
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

    /**
     * O que o usuário logado pode ver e alterar (RN19). Sem ninguém logado (telas abertas
     * direto pelo NetBeans, durante o desenvolvimento), o acesso é total.
     */
    public static Acesso getAcesso() {
        return acesso == null ? Acesso.administrador() : acesso;
    }

    /** Indica se o usuário logado pode cadastrar outros usuários (somente administradores). */
    public static boolean podeGerenciarUsuarios() {
        return usuarioLogado != null && getAcesso().podeGerenciarUsuarios();
    }
}
