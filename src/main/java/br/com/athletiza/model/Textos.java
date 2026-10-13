package br.com.athletiza.model;

import java.text.Collator;
import java.util.Locale;

/**
 * Comparações usadas na ordenação das classes de modelo.
 *
 * Textos em português ignoram maiúsculas/minúsculas e ordenam acentos
 * corretamente ("Álvaro" fica junto de "Alice"). Valores nulos ficam no final.
 */
final class Textos {

    private static final Collator COLLATOR = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    static {
        COLLATOR.setStrength(Collator.SECONDARY);
    }

    private Textos() {
    }

    /** Compara dois textos em ordem alfabética; nulos ficam no final. */
    static int comparar(String a, String b) {
        if (a == null || b == null) {
            return compararNulos(a, b);
        }
        return COLLATOR.compare(a, b);
    }

    /** Compara dois valores pela ordem natural deles (compareTo); nulos ficam no final. */
    static <T extends Comparable<T>> int compararNulosNoFim(T a, T b) {
        if (a == null || b == null) {
            return compararNulos(a, b);
        }
        return a.compareTo(b);
    }

    /** Usado quando pelo menos um dos dois é nulo: o nulo fica depois. */
    private static int compararNulos(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        }
        return a == null ? 1 : -1;
    }
}
