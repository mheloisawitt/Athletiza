package br.com.athletiza.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Cargo ou função na gestão (RF07). A "ordem" define o nível no organograma
 * (1 = topo) e o cargo superior define a quem ele responde (RF08).
 */
public class Cargo extends Entidade implements Comparable<Cargo> {

    private static final Comparator<Cargo> ORDEM_NATURAL = Comparator.comparingInt(Cargo::getOrdem)
            .thenComparing(Cargo::getNome, Textos.COMPARADOR);

    private String nome;
    private int ordem;
    private Cargo cargoSuperior;

    public Cargo() {
    }

    public Cargo(String nome, int ordem, Cargo cargoSuperior) {
        this.nome = nome;
        this.ordem = ordem;
        this.cargoSuperior = cargoSuperior;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public Cargo getCargoSuperior() {
        return cargoSuperior;
    }

    public void setCargoSuperior(Cargo cargoSuperior) {
        this.cargoSuperior = cargoSuperior;
    }

    @Override
    public int compareTo(Cargo outro) {
        return ORDEM_NATURAL.compare(this, outro);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cargo outro) || nome == null) {
            return false;
        }
        return nome.equalsIgnoreCase(outro.nome);
    }

    @Override
    public int hashCode() {
        return nome == null ? System.identityHashCode(this) : Objects.hash(nome.toLowerCase());
    }

    @Override
    public String toString() {
        return nome;
    }
}
