package br.com.athletiza.model;

import java.util.Objects;

/**
 * Usuário que acessa o sistema (RF01). A senha nunca é guardada em texto
 * puro, apenas o hash.
 */
public class Usuario extends Pessoa {

    private String login;
    private String senhaHash;
    private Perfil perfil = Perfil.CONSULTA;
    private boolean ativo = true;

    public Usuario() {
    }

    public Usuario(String nome, String login, Perfil perfil) {
        setNome(nome);
        this.login = login;
        this.perfil = perfil;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /** Usuários são identificados pelo login, não pela matrícula. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario outro) || login == null) {
            return false;
        }
        return login.equalsIgnoreCase(outro.login);
    }

    @Override
    public int hashCode() {
        return login == null ? System.identityHashCode(this) : Objects.hash(login.toLowerCase());
    }
}
