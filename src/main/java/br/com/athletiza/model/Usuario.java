package br.com.athletiza.model;

import java.util.Objects;

public class Usuario extends Pessoa {
    private String login;
    private String senhaHash;
    private boolean administrador;
    private Membro membro;
    private boolean ativo = true;
    private boolean deveTrocarSenha;

    public Usuario() {
    }

    public Usuario(String nome, String login) {
        setNome(nome);
        this.login = login;
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

    public boolean isAdministrador() {
        return administrador;
    }

    public void setAdministrador(boolean administrador) {
        this.administrador = administrador;
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isDeveTrocarSenha() {
        return deveTrocarSenha;
    }

    public void setDeveTrocarSenha(boolean deveTrocarSenha) {
        this.deveTrocarSenha = deveTrocarSenha;
    }

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
