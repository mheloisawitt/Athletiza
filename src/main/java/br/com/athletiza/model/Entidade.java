package br.com.athletiza.model;

/**
 * Classe base de tudo que é gravado no banco: possui um identificador.
 */
public abstract class Entidade {

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /** Indica se a entidade ainda não foi gravada no banco. */
    public boolean isNova() {
        return id == null;
    }
}
