package br.com.athletiza.modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

public abstract class Atividade extends Entidade implements Comparable<Atividade> {
    public static final Comparator<Atividade> POR_DATA = Comparator
            .comparing(Atividade::getData, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Atividade::getHorario, Comparator.nullsFirst(Comparator.naturalOrder()));

    private String titulo;
    private LocalDate data;
    private LocalTime horario;
    private String local;
    private String observacoes;

    protected Atividade() {
    }

    protected Atividade(String titulo, LocalDate data, LocalTime horario, String local) {
        this.titulo = titulo;
        this.data = data;
        this.horario = horario;
        this.local = local;
    }

    public abstract TipoAtividade getTipo();

    public String getDescricaoCalendario() {
        return getTipo().getDescricao() + " - " + titulo;
    }

    public String getRotuloCurto() {
        return titulo;
    }

    public String getCorHex() {
        return getTipo().getCorHex();
    }

    public boolean ocorreEm(LocalDate dia) {
        return data != null && data.equals(dia);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @Override
    public int compareTo(Atividade outra) {
        return POR_DATA.compare(this, outra);
    }

    @Override
    public String toString() {
        return titulo;
    }
}
