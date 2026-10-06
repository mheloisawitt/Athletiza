package br.com.athletiza.model;

/**
 * Perfis de acesso ao sistema (RN19).
 */
public enum Perfil {

    ADMINISTRADOR("Administrador", true, true),
    DIRETORIA("Diretoria", true, false),
    CONSULTA("Consulta", false, false);

    private final String descricao;
    private final boolean podeAlterarDados;
    private final boolean podeGerenciarUsuarios;

    Perfil(String descricao, boolean podeAlterarDados, boolean podeGerenciarUsuarios) {
        this.descricao = descricao;
        this.podeAlterarDados = podeAlterarDados;
        this.podeGerenciarUsuarios = podeGerenciarUsuarios;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Pode incluir, editar e excluir registros. */
    public boolean podeAlterarDados() {
        return podeAlterarDados;
    }

    public boolean podeGerenciarUsuarios() {
        return podeGerenciarUsuarios;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
