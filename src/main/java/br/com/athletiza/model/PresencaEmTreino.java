package br.com.athletiza.model;

/**
 * Linha do histórico de um atleta: o treino e se ele esteve presente (RF13).
 */
public record PresencaEmTreino(Treino treino, Presenca presenca) {
}
