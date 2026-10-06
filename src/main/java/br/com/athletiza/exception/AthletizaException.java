package br.com.athletiza.exception;

/**
 * Exceção base do sistema. Toda exceção personalizada estende esta classe,
 * permitindo que as telas tratem qualquer erro do sistema em um único catch.
 *
 * A mensagem deve ser compreensível para o usuário final (RN12).
 */
public abstract class AthletizaException extends Exception {

    protected AthletizaException(String mensagem) {
        super(mensagem);
    }

    protected AthletizaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
