package br.com.athletiza.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Compromisso ou ação avulsa no calendário, como uma reunião de gestão (RF09).
 */
public class Compromisso extends Atividade {

    public Compromisso() {
    }

    public Compromisso(String titulo, LocalDate data, LocalTime horario, String local) {
        super(titulo, data, horario, local);
    }

    @Override
    public TipoAtividade getTipo() {
        return TipoAtividade.COMPROMISSO;
    }

    @Override
    public String getDescricaoCalendario() {
        return getTitulo();
    }
}
