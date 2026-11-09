package br.com.athletiza.model;

import java.util.List;
import java.util.Objects;

/**
 * Dados de um gráfico com categorias no eixo horizontal (meses, modalidades...)
 * e uma ou mais séries de valores, uma para cada categoria.
 *
 * @param categorias rótulos do eixo horizontal, na ordem de exibição
 * @param series     cada série tem um valor por categoria; valor nulo = sem dado naquela categoria
 */
public record Comparativo(List<String> categorias, List<Serie> series) {

    /** Uma linha (ou cor de coluna) do gráfico. */
    public record Serie(String nome, List<Integer> valores) {

        /** Valor da categoria, ou 0 se não houver dado. */
        public int valor(int categoria) {
            Integer valor = valores.get(categoria);
            return valor == null ? 0 : valor;
        }
    }

    /** Verdadeiro quando não há nenhum valor maior que zero. */
    public boolean isVazio() {
        return series.stream().flatMap(s -> s.valores().stream())
                .filter(Objects::nonNull).noneMatch(v -> v > 0);
    }

    public Serie serie(String nome) {
        return series.stream().filter(s -> s.nome().equals(nome)).findFirst().orElseThrow();
    }
}
