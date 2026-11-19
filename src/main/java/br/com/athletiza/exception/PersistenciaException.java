package br.com.athletiza.exception;

public class PersistenciaException extends AthletizaException {
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
