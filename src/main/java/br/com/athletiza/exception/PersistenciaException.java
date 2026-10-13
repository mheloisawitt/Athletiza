package br.com.athletiza.exception;

/**
 * Lançada quando ocorre falha no acesso ao banco de dados.
 *
 * A mensagem é amigável; o erro técnico original (SQLException) fica na causa
 * e deve ir apenas para o log.
 */
public class PersistenciaException extends AthletizaException {

    public PersistenciaException() {
        super("Erro ao acessar o banco de dados. Tente novamente.");
    }

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
