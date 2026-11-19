package br.com.athletiza.excecao;

public class PersistenciaException extends AthletizaException {
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
