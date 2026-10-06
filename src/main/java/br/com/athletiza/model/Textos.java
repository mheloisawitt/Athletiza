package br.com.athletiza.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Comparação de textos em português: ignora maiúsculas/minúsculas e
 * ordena acentos corretamente ("Álvaro" fica junto de "Alice").
 */
final class Textos {

    static final Comparator<String> COMPARADOR = criarComparador();

    private Textos() {
    }

    private static Comparator<String> criarComparador() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        collator.setStrength(Collator.SECONDARY);
        return Comparator.nullsLast(collator::compare);
    }
}
