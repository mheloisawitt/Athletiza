package br.com.athletiza.model;

/**
 * Membro da atlética: ocupa cargos nas gestões, organiza eventos e
 * é responsável por tarefas e treinos (RF02).
 */
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
