package br.com.athletiza.modelo;

import java.util.List;
import java.util.Objects;

public record Comparativo(List<String> categorias, List<Serie> series) {
    public record Serie(String nome, List<Integer> valores) {
        public int valor(int categoria) {
            Integer valor = valores.get(categoria);
            return valor == null ? 0 : valor;
        }
    }

    public boolean isVazio() {
        return series.stream().flatMap(s -> s.valores().stream())
                .filter(Objects::nonNull).noneMatch(v -> v > 0);
    }

    public Serie serie(String nome) {
        return series.stream().filter(s -> s.nome().equals(nome)).findFirst().orElseThrow();
    }
}
