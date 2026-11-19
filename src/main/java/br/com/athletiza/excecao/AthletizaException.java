package br.com.athletiza.excecao;

public abstract class AthletizaException extends Exception {
    protected AthletizaException(String mensagem) {
        super(mensagem);
    }

    protected AthletizaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
