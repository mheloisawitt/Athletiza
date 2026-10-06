package br.com.athletiza.exception;

/**
 * Lançada quando uma operação viola uma regra do sistema
 * (ex.: períodos de gestão sobrepostos, matrícula já cadastrada,
 * exclusão de registro com vínculos).
 */
public class RegraNegocioException extends AthletizaException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
