package br.com.athletiza.modelo;

public class Membro extends Pessoa {
    private Situacao situacao = Situacao.ATIVO;

    public Membro() {
    }

    public Membro(String nome, String matricula, String contato) {
        super(nome, matricula, contato);
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }
}
