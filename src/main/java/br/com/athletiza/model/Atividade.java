package br.com.athletiza.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

/**
 * Qualquer atividade que aparece no calendário: treino, amistoso,
 * competição, evento ou compromisso (CH-08).
 *
 * O calendário trabalha só com Atividade; cada subclasse informa o seu
 * tipo, cor e descrição (polimorfismo). Ordem natural: data e horário.
 */
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

    /** Tipo usado para agrupar e colorir a atividade no calendário. */
    public abstract TipoAtividade getTipo();

    /** Texto curto exibido no calendário e na lista de próximos eventos. */
    public String getDescricaoCalendario() {
        return getTipo().getDescricao() + " - " + titulo;
    }

    public String getCorHex() {
        return getTipo().getCorHex();
    }

    /** Indica se a atividade acontece no dia informado. */
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
