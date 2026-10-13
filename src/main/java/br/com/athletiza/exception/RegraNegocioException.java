package br.com.athletiza.exception;

/**
 * Lançada quando uma operação viola uma regra do sistema
 * (ex.: períodos de gestão sobrepostos, matrícula já cadastrada,
 * exclusão de registro com vínculos).
 *
 * Exceção verificada (checked): quem chama é obrigado a tratar ou propagar.
 */
public class RegraNegocioException extends AthletizaException {

    public RegraNegocioException() {
        super("A operação não é permitida pelas regras do sistema.");
    }

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
