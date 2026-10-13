package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
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

    /** Regras de preenchimento (CH-16). */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(getTitulo(), "Título")
                .tamanhoMaximo(getTitulo(), 100, "Título")
                .obrigatorio(getData(), "Data")
                .tamanhoMaximo(getLocal(), 100, "Local")
                .tamanhoMaximo(getObservacoes(), 500, "Observações")
                .validar();
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
