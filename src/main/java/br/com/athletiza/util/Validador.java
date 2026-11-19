package br.com.athletiza.util;

import br.com.athletiza.excecao.ValidacaoException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Validador {
    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter FORMATO_HORARIO = DateTimeFormatter.ofPattern("HH:mm");

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern TELEFONE = Pattern.compile("^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$");
    private static final Pattern NUMEROS = Pattern.compile("^\\d+$");

    private final List<String> erros = new ArrayList<>();

    public Validador obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            erros.add("O campo \"" + campo + "\" é obrigatório.");
        }
        return this;
    }

    public Validador obrigatorio(Object valor, String campo) {
        if (valor == null) {
            erros.add("O campo \"" + campo + "\" é obrigatório.");
        }
        return this;
    }

    public Validador tamanhoMaximo(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            erros.add("O campo \"" + campo + "\" deve ter no máximo " + maximo + " caracteres.");
        }
        return this;
    }

    public Validador apenasNumeros(String valor, String campo) {
        if (valor != null && !valor.isBlank() && !NUMEROS.matcher(valor.trim()).matches()) {
            erros.add("O campo \"" + campo + "\" deve conter apenas números.");
        }
        return this;
    }

    public Validador contato(String valor, String campo) {
        if (valor != null && !valor.isBlank()) {
            String texto = valor.trim();
            if (!EMAIL.matcher(texto).matches() && !TELEFONE.matcher(texto).matches()) {
                erros.add("O campo \"" + campo + "\" deve ser um telefone com DDD ou um e-mail válido.");
            }
        }
        return this;
    }

    public Validador periodo(LocalDate inicio, LocalDate fim, String campoInicio, String campoFim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            erros.add("A \"" + campoFim + "\" não pode ser anterior à \"" + campoInicio + "\".");
        }
        return this;
    }

    public Validador regra(boolean valido, String mensagem) {
        if (!valido) {
            erros.add(mensagem);
        }
        return this;
    }

    public boolean temErros() {
        return !erros.isEmpty();
    }

    public void validar() throws ValidacaoException {
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    public static LocalDate converterData(String texto, String campo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("O campo \"" + campo + "\" deve ser uma data válida no formato dd/mm/aaaa.");
        }
    }

    public static LocalTime converterHorario(String texto, String campo) throws ValidacaoException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(texto.trim(), FORMATO_HORARIO);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("O campo \"" + campo + "\" deve ser um horário válido no formato hh:mm.");
        }
    }
}
