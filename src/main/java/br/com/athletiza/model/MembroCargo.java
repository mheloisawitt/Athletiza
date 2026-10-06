package br.com.athletiza.model;

/**
 * Vínculo de um membro a um cargo dentro de uma gestão.
 * Por ser um record, dois vínculos com o mesmo membro e cargo são iguais,
 * então o Set da gestão impede duplicidade (CH-21).
 */
public record MembroCargo(Membro membro, Cargo cargo) {
}
