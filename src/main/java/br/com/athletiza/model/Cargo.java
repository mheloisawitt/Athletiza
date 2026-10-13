package br.com.athletiza.model;

import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import java.util.Objects;

/**
 * Cargo ou função na gestão (RF07). A "ordem" define o nível no organograma
 * (1 = topo) e o cargo superior define a quem ele responde (RF08).
 */
public class Cargo extends Entidade implements Comparable<Cargo> {

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

    /** Regras de preenchimento do cadastro (CH-18). */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(nome, "Nome")
                .tamanhoMaximo(nome, 60, "Nome")
                .regra(ordem >= 1, "O \"Nível\" deve ser 1 ou maior (1 = topo do organograma).")
                .validar();
    }

    /**
     * Confere a hierarquia (RF07): o superior precisa ter nível menor e
     * um cargo não pode ficar subordinado a si mesmo.
     */
    public void verificarHierarquia() throws RegraNegocioException {
        if (cargoSuperior == null) {
            return;
        }
        if (cargoSuperior.getOrdem() >= ordem) {
            throw new RegraNegocioException("O cargo superior (" + cargoSuperior + ", nível " + cargoSuperior.getOrdem()
                    + ") deve ter nível menor que o deste cargo.");
        }
        for (Cargo acima = cargoSuperior; acima != null; acima = acima.getCargoSuperior()) {
            if (!isNova() && getId().equals(acima.getId())) {
                throw new RegraNegocioException("Um cargo não pode ficar subordinado a si mesmo.");
            }
        }
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

    /** Ordem natural: pelo nível no organograma e, empatando, pelo nome. */
    @Override
    public int compareTo(Cargo outro) {
        if (ordem != outro.ordem) {
            return Integer.compare(ordem, outro.ordem);
        }
        return Textos.comparar(nome, outro.nome);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cargo) || nome == null) {
            return false;
        }
        Cargo outro = (Cargo) o;
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
