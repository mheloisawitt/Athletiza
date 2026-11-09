package br.com.athletiza.model;

/**
 * Perfis de acesso ao sistema (RN19). Fora o administrador, o perfil vem do
 * cargo que o membro ligado ao usuário ocupa na gestão em andamento.
 */
public enum Perfil {

    /** Acesso total, inclusive cadastro de usuários. */
    ADMINISTRADOR("Administrador"),
    /** Presidente ou vice: inclui, edita e exclui em todos os módulos, menos usuários. */
    PRESIDENCIA("Presidência"),
    /** Consulta tudo; inclui e edita só o que é da área da sua diretoria. */
    DIRETOR("Diretor"),
    /** Somente consulta, apenas dos módulos do setor que assessora. */
    ASSESSOR("Assessor");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
