package br.com.athletiza.model;

public abstract class Entidade {
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isNova() {
        return id == null;
    }
}
