package br.com.athletiza.model;

import java.util.Objects;

/**
 * Vínculo de um membro a um cargo dentro de uma gestão.
 * Dois vínculos com o mesmo membro e cargo são iguais (equals e hashCode),
 * então o Set da gestão impede duplicidade (CH-21).
 *
 * Ordem natural: pelo cargo (nível e nome) e, no mesmo cargo, pelo nome do membro.
 */
public class MembroCargo implements Comparable<MembroCargo> {

    private final Membro membro;
    private final Cargo cargo;

    public MembroCargo(Membro membro, Cargo cargo) {
        this.membro = membro;
        this.cargo = cargo;
    }

    public Membro getMembro() {
        return membro;
    }

    public Cargo getCargo() {
        return cargo;
    }

    @Override
    public int compareTo(MembroCargo outro) {
        int resultado = cargo.compareTo(outro.cargo);
        return resultado != 0 ? resultado : Textos.comparar(membro.getNome(), outro.membro.getNome());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MembroCargo)) {
            return false;
        }
        MembroCargo outro = (MembroCargo) o;
        return Objects.equals(membro, outro.membro) && Objects.equals(cargo, outro.cargo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(membro, cargo);
    }

    @Override
    public String toString() {
        return membro + " - " + cargo;
    }
}
