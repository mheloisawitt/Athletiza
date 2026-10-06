package br.com.athletiza.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Amistoso: um treino contra um adversário, com resultado (CH-35).
 */
public class Amistoso extends Treino {

    private String adversario;
    private String resultado;

    public Amistoso() {
    }

    public Amistoso(Modalidade modalidade, String adversario, LocalDate data, LocalTime horario, String local) {
        super(modalidade, data, horario, local);
        this.adversario = adversario;
        setTitulo("Amistoso " + modalidade + " x " + adversario);
    }

    @Override
    public TipoAtividade getTipo() {
        return TipoAtividade.AMISTOSO;
    }

    public String getAdversario() {
        return adversario;
    }

    public void setAdversario(String adversario) {
        this.adversario = adversario;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
