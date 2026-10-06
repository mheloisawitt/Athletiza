package br.com.athletiza.util;

import br.com.athletiza.exception.ValidacaoException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validação dos dados informados nas telas (RN13).
 *
 * Acumula todos os erros e lança uma única ValidacaoException no final,
 * para o usuário ver tudo o que precisa corrigir de uma vez:
 *
 * <pre>
 * new Validador()
 *         .obrigatorio(nome, "Nome")
 *         .obrigatorio(dataInicio, "Data de início")
 *         .periodo(dataInicio, dataFim, "Data de início", "Data de fim")
 *         .validar();
 * </pre>
 */
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

    /** Só números (ex.: matrícula). Campo vazio é ignorado; use obrigatorio() junto se necessário. */
    public Validador apenasNumeros(String valor, String campo) {
        if (valor != null && !valor.isBlank() && !NUMEROS.matcher(valor.trim()).matches()) {
            erros.add("O campo \"" + campo + "\" deve conter apenas números.");
        }
        return this;
    }

    /** Aceita telefone com DDD ou e-mail. Campo vazio é ignorado. */
    public Validador contato(String valor, String campo) {
        if (valor != null && !valor.isBlank()) {
            String texto = valor.trim();
            if (!EMAIL.matcher(texto).matches() && !TELEFONE.matcher(texto).matches()) {
                erros.add("O campo \"" + campo + "\" deve ser um telefone com DDD ou um e-mail válido.");
            }
        }
        return this;
    }

    /** A data final não pode ser anterior à inicial. Datas nulas são ignoradas. */
    public Validador periodo(LocalDate inicio, LocalDate fim, String campoInicio, String campoFim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            erros.add("A \"" + campoFim + "\" não pode ser anterior à \"" + campoInicio + "\".");
        }
        return this;
    }

    /** Adiciona um erro de uma regra específica da tela. */
    public Validador regra(boolean valido, String mensagem) {
        if (!valido) {
            erros.add(mensagem);
        }
        return this;
    }

    public boolean temErros() {
        return !erros.isEmpty();
    }

    /** Lança ValidacaoException com todos os erros encontrados, se houver. */
    public void validar() throws ValidacaoException {
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    /**
     * Converte o texto digitado (dd/mm/aaaa) em data.
     *
     * @return null se o texto estiver vazio
     */
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

    /**
     * Converte o texto digitado (hh:mm) em horário.
     *
     * @return null se o texto estiver vazio
     */
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
