package br.com.athletiza.excecao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
