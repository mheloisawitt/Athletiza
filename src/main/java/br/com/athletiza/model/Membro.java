package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;

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

    /** Regras de preenchimento do cadastro (CH-22). */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(getNome(), "Nome")
                .tamanhoMaximo(getNome(), 100, "Nome")
                .obrigatorio(getMatricula(), "Matrícula")
                .apenasNumeros(getMatricula(), "Matrícula")
                .tamanhoMaximo(getMatricula(), 20, "Matrícula")
                .contato(getContato(), "Contato")
                .tamanhoMaximo(getContato(), 100, "Contato")
                .obrigatorio(situacao, "Situação")
                .validar();
    }

    public boolean isAtivo() {
        return situacao == Situacao.ATIVO;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }
}
