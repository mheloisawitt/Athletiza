package br.com.athletiza.util;

import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Usuario;

/**
 * Guarda o usuário logado durante o uso do sistema.
 */
public final class Sessao {

    private static Usuario usuarioLogado;

    private Sessao() {
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void iniciar(Usuario usuario) {
        usuarioLogado = usuario;
    }

    public static void encerrar() {
        usuarioLogado = null;
    }

    /** Indica se o usuário logado pode incluir, editar e excluir (CH-13). */
    public static boolean podeAlterarDados() {
        return usuarioLogado != null && usuarioLogado.getPerfil().podeAlterarDados();
    }

    public static Perfil getPerfil() {
        return usuarioLogado == null ? null : usuarioLogado.getPerfil();
    }
}
