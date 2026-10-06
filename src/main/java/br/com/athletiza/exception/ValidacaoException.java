package br.com.athletiza.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lançada quando dados informados pelo usuário são inválidos
 * (campo obrigatório vazio, data inválida, formato incorreto...).
 *
 * Guarda todos os erros encontrados para que a tela mostre de uma vez.
 */
public class ValidacaoException extends AthletizaException {

    private final List<String> erros;

    public ValidacaoException(String erro) {
        this(List.of(erro));
    }

    public ValidacaoException(List<String> erros) {
        super(String.join("\n", erros));
        this.erros = new ArrayList<>(erros);
    }

    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }
}
